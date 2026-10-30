# Zingy

**A downloader for the thousand sites yt-dlp knows about — as an Android app and as a web app.**

Zingy takes a link and gives you the file. YouTube, Instagram, TikTok, Twitter/X,
Facebook, Reddit, Twitch, Vimeo, SoundCloud — anything [yt-dlp](https://github.com/yt-dlp/yt-dlp)
supports, which is well over a thousand sites. Pick a format, watch it download,
keep the file.

Two frontends, one engine:

- **Android app** — Kotlin + Jetpack Compose, with yt-dlp running *on the phone*
  through Chaquopy. No server, no account, works from anywhere.
- **Web app** — Flask, for a browser on any machine on the network.

[![Download](https://img.shields.io/badge/APK-Releases-3ddc84?logo=android&logoColor=white)](../../releases)
![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white)
![Flask](https://img.shields.io/badge/Flask-yt--dlp-000?logo=flask)

---

## The Android app

Package `dev.ayaanngandhi.zingy`. Android 8.0 and up.

Grab the APK from [Releases](../../releases), or build it:

```bash
./build_apk.sh                 # or: ./gradlew assembleRelease
# → app/build/outputs/apk/release/
```

The release build is signed with `zingy-release.keystore`.

**How it works.** The APK embeds a Python interpreter through Chaquopy and pip-installs
yt-dlp into it. `app/src/main/python/downloader.py` is the bridge between Kotlin and
yt-dlp; `DownloadService.kt` runs downloads as a foreground service so they survive
the app being backgrounded. The download folder is configurable, and a saved file is
just a file — it plays offline like anything else in your storage. Nothing leaves the
phone.

**Build requirements, and why each one bites:**

| Requirement | If you get it wrong |
| --- | --- |
| JDK 17 | Gradle fails on plugin resolution with an unhelpful version error |
| Android SDK | `sdk.dir` missing from `local.properties` |
| Chaquopy `buildPython = /usr/bin/python3.12` | Must match an interpreter that exists on the build machine |
| Chaquopy Python target **≥ 3.11** | On 3.8, pip caps yt-dlp at 2024.10.22 and `assembleDebug` produces no APK at all |

`build_apk.sh` exports `JAVA_HOME` and `ANDROID_HOME` for you. Check the path inside it
points at your checkout — it is a script written for one machine.

---

## The web app

A Flask server around the same yt-dlp. Open it, paste a link, choose a format, download.

```bash
cd webapp
python3 -m venv venv && ./venv/bin/pip install -r requirements.txt
./venv/bin/python app.py            # http://localhost:4321
```

| Route | What it does |
| --- | --- |
| `GET /` | The page |
| `POST /api/formats` | Inspect a URL, return the formats worth offering |
| `POST /api/download` | Start a download, return an id |
| `GET /api/progress/<id>` | Poll progress while it runs |
| `GET /api/file/<id>` | Collect the finished file |
| `GET /api/stats` | Counters |
| `GET /terms`, `/privacy` | The boring bits |

**Downloads stream to the browser and are never stored.** The file is written to a
temp path, streamed to the client, then deleted; a download nobody collects is swept
after a timeout so a burst of requests cannot fill the disk. `webapp/visit_logs.json`
does record each request (IP, user agent, path) — that is a visitor log, not a library
of what anyone downloaded.

Configuration is environment, not code: `PORT` (default 4321) and `YTDLP_COOKIES`
(point it at a cookies file when a site demands a login).

> Extractor workarounds are load-bearing. YouTube needs
> `extractor_args={"youtube": {"player_client": ["android", "web"]}}` and Instagram
> breaks if you leave DASH on. Removing them looks like a simplification and is not.

---

## Hosting the web app on the LAN

The same six steps as every other app on the home server. Ports and names change;
the shape does not.

### 1. One port

Zingy is a single Flask process: **4321**. Check it is free (`ss -tln | grep :4321`).

### 2. A systemd user unit

```ini
# ~/.config/systemd/user/zingy.service
[Unit]
Description=Zingy video downloader webapp (Flask, :4321)
After=network-online.target
Wants=network-online.target

[Service]
WorkingDirectory=/home/mac/production/zingy/webapp
ExecStart=/home/mac/production/zingy/webapp/venv/bin/python app.py
Restart=always
RestartSec=3

[Install]
WantedBy=default.target
```

```bash
systemctl --user daemon-reload
systemctl --user enable --now zingy.service
loginctl enable-linger "$USER"
```

Two details worth copying: `ExecStart` points at the **venv's** python by absolute
path (a unit inherits a minimal `PATH`, so `python` may not be the one with Flask
installed), and `Restart=always` means a crash recovers on its own — unlike a
`oneshot` launcher, there is nothing left unsupervised.

**Serve the production copy, not the checkout.** Develop in one directory, run from
another, and restart the unit after syncing — a hand-started `python app.py` and the
unit will fight over the port, and whichever started second loses.

### 3. A name, in Pi-hole

```toml
# /etc/pihole/pihole.toml → [dns]
hosts = [ "192.168.68.129 zingy.mac" ]
```

```bash
docker restart pihole && dig +short @192.168.68.129 zingy.mac
```

### 4. A route in Atrium

Port 80 belongs to [Atrium](https://github.com/ayaanngandhi/atrium), a small router
that forwards `/<service>` to the port that service runs on:

```json
{ "slug": "zingy", "port": 4321, "name": "Zingy", "note": "Video downloader" }
```

in `~/atrium/config.json`. It re-reads the config on save — no restart, no vhost.

### 5. A vhost, if you want a `.mac` name on port 80

```nginx
# /etc/nginx/sites-available/zingy.conf
server {
    listen 8081;                     # Atrium owns :80, nginx sits behind it
    server_name zingy.mac;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:4321;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 600s;     # a long download must not be cut at 60s
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/zingy.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

The timeout is the one that matters here. nginx's default 60-second read timeout
kills a slow download that is otherwise working perfectly.

### 6. Verify every layer

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:4321/                        # the app
curl -s -o /dev/null -w '%{http_code}\n' -H 'Host: zingy.mac' http://127.0.0.1:8081/   # nginx
dig +short @192.168.68.129 zingy.mac                                                   # DNS
curl -sL http://192.168.68.129/zingy | grep -o '<title>[^<]*'                           # Atrium
```

| URL | Route |
| --- | --- |
| `http://192.168.68.129/zingy` | Atrium :80 → redirect to :4321 |
| `http://zingy.mac/` | Pi-hole → nginx :8081 → :4321 |
| `http://192.168.68.129:4321` | straight to Flask |

A 200 from the port alone is not proof the deploy worked — that can be a stale
process holding the port from an earlier session. Check the listener's PID.

---

## Layout

```
Zingy-Downloader/
├── app/src/main/
│   ├── java/dev/ayaanngandhi/zingy/   MainActivity · DownloadService · ViewModel
│   ├── python/downloader.py           Kotlin ↔ yt-dlp bridge
│   └── res/                           themes, icons
├── webapp/
│   ├── app.py                         Flask server, yt-dlp backend
│   ├── templates/                     index · privacy · terms
│   └── static/                        zingy.css · zingy.js
├── build_apk.sh · install_dependencies.sh
└── docs/                              UI and build notes
```

## License

MIT — see [LICENSE](LICENSE).

Zingy is a front end for yt-dlp, which is where essentially all of the hard work
lives. Respect the terms of service of the sites you use it on, and please do not
use it to redistribute other people's work.
