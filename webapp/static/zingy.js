/* Zingy front end - The Cutting Room.
   Markup-only template + this file + zingy.css. API contract is unchanged. */
(function () {
  "use strict";

  /* Existing brand marks, kept as-is (no generic icons, no emoji). */
  var PLATFORM_ICONS = {
    youtube: '<svg viewBox="0 0 24 24" fill="#ff0000"><path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/></svg>',
    instagram: '<svg viewBox="0 0 24 24" fill="#c13584"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/></svg>',
    tiktok: '<svg viewBox="0 0 24 24" fill="#25F4EE"><path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07z"/></svg>',
    twitter: '<svg viewBox="0 0 24 24" fill="#ECE8E1"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>',
    facebook: '<svg viewBox="0 0 24 24" fill="#0866ff"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>',
    reddit: '<svg viewBox="0 0 24 24" fill="#ff4500"><path d="M12 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0zm5.01 4.744c.688 0 1.25.561 1.25 1.249a1.25 1.25 0 0 1-2.498.056l-2.597-.547.8-3.747c-2.06-.09-4.12-.09-6.18 0l.8 3.747-2.597.547a1.25 1.25 0 0 1-1.061-1.856 1.25 1.25 0 0 1 2.007-.293 10.7 10.7 0 0 1 5.73-1.17 10.7 10.7 0 0 1 5.73 1.17 1.25 1.25 0 0 1 .616-.156zM12 15.5c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm5.5-2c0 .55-.45 1-1 1s-1-.45-1-1 .45-1 1-1 1 .45 1 1zm-11 0c0 .55-.45 1-1 1s-1-.45-1-1 .45-1 1-1 1 .45 1 1zm9.83 4.17c-.31.51-.97.66-1.48.35-.65-.39-1.83-.91-3.35-1.02.15.73.37 1.45.64 2.12.16.39.07.82-.28 1.01-.81.43-2.16.43-2.97 0-.35-.19-.44-.62-.28-1.01.27-.67.49-1.39.64-2.12-1.52.11-2.7.63-3.35 1.02-.51.31-1.17.16-1.48-.35-.31-.51-.16-1.17.35-1.48 1.1-.67 2.55-1.08 4.59-1.08 2.04 0 3.49.41 4.59 1.08.51.31.66.97.35 1.48z"/></svg>',
    twitch: '<svg viewBox="0 0 24 24" fill="#9146ff"><path d="M11.571 4.714h1.715v5.143H11.57zm4.715 0H18v5.143h-1.714zM6 0L1.714 4.286v15.428h5.143V24l4.286-4.286h3.428L22.286 12V0H6zm14.571 11.143l-3.428 3.428h-3.429l-3 3v-3H6.857V1.714h13.714v9.429z"/></svg>',
    vimeo: '<svg viewBox="0 0 24 24" fill="#1ab7ea"><path d="M23.977 6.416c-.105 2.338-1.739 5.543-4.894 9.609-3.268 4.247-6.026 6.37-8.29 6.37-1.409 0-2.578-1.294-3.553-3.881L5.322 11.4C4.603 8.816 3.834 7.522 3.01 7.522c-.179 0-.806.378-1.881 1.132L0 7.197c1.185-1.044 2.351-2.084 3.501-3.128C5.08 2.701 6.266 1.984 7.055 1.91c1.867-.18 3.016 1.1 3.447 3.838.465 2.953.789 4.789.971 5.507.539 2.45 1.131 3.674 1.776 3.674.502 0 1.256-.796 2.265-2.385 1.004-1.589 1.54-2.797 1.612-3.628.144-1.371-.395-2.061-1.614-2.061-.574 0-1.167.121-1.777.391 1.186-3.868 3.434-5.757 6.762-5.637 2.473.06 3.628 1.664 3.493 4.797z"/></svg>',
    soundcloud: '<svg viewBox="0 0 24 24" fill="#ff5500"><path d="M1.175 12.725l.386-2.472.773 2.472-.659 2.758zm2.178-3.612l.509 6.12-.559 2.707-.609-6.119zm2.279-1.802l.762 7.875-.711 2.815-.762-7.874zm2.152-.509l.965 8.282-.813 2.77-.914-8.279zm8.666-2.768c2.248 0 4.091 1.528 4.091 3.593 0 .152-.015.302-.043.449h.043c3.121 0 5.459 2.252 5.459 4.938 0 2.62-2.338 4.688-5.459 4.688H8.935c-.738-.017-1.315-.593-1.315-1.332v-.031c0-.697.565-1.265 1.262-1.265h.053l7.203-.009c.891-.009 1.607-.749 1.607-1.625 0-.885-.73-1.609-1.621-1.609h-.459c-.527 0-.969-.387-1.051-.897-.113-.698-.733-1.219-1.445-1.219-.443 0-.842.192-1.118.497-.275-.036-.518.084-.792.19-.058-.664-.537-1.225-1.151-1.358z"/></svg>',
    spotify: '<svg viewBox="0 0 24 24" fill="#1ed760"><path d="M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z"/></svg>',
    bilibili: '<svg viewBox="0 0 24 24" fill="#00aeec"><path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.774l-1.174-1.12a1.234 1.234 0 0 1-.373-.906c0-.356.124-.658.373-.907l.027-.027c.267-.249.573-.373.92-.373.347 0 .653.124.92.373L9.653 4.44c.071.071.134.142.187.213h4.267a.836.836 0 0 1 .16-.213l2.853-2.747c.267-.249.573-.373.92-.373.347 0 .662.151.929.4.267.249.391.551.391.907 0 .355-.124.657-.373.906zM5.333 7.24c-.746.018-1.373.276-1.88.773-.506.498-.769 1.13-.786 1.894v7.52c.017.764.28 1.396.786 1.894.507.498 1.134.756 1.88.773h13.334c.746-.017 1.373-.275 1.88-.773.506-.498.769-1.13.786-1.894v-7.52c-.017-.765-.28-1.396-.786-1.894-.507-.497-1.134-.755-1.88-.773H5.333zM8 11.107c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c0-.373.129-.689.386-.947.258-.257.574-.386.947-.386zm8 0c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c.017-.391.15-.711.4-.96.249-.249.56-.373.933-.373z"/></svg>',
    dailymotion: '<svg viewBox="0 0 24 24" fill="#00d2ff"><path d="M14.068 11.313c-1.754 0-3.104 1.427-3.104 3.11 0 1.753 1.35 3.085 3.104 3.085 1.521 0 2.772-1.022 3.011-2.688h-3.041v-1.345h4.481c.091.346.138.698.138 1.057 0 3.063-2.233 5.218-5.239 5.218-3.054 0-5.391-2.145-5.391-5.327 0-3.104 2.337-5.432 5.391-5.432 1.437 0 2.745.539 3.714 1.42l-.972 1.261c-.753-.672-1.726-1.07-2.092-1.07v-.289zm8.836-7.013c.311.168.436.521.269.832l-1.045 1.942c-1.242-1.029-2.768-1.576-4.41-1.576-3.784 0-6.853 2.949-6.853 6.573 0 3.645 3.069 6.553 6.853 6.553 1.642 0 3.168-.547 4.41-1.576l1.045 1.942c.167.311.562.468.832.269.311-.168.468-.521.269-.832l-1.045-1.942c1.357-1.215 2.239-2.979 2.239-4.929 0-3.605-1.896-6.783-4.698-8.466l1.045-1.942c.199-.311.656-.357.889-.188zm-4.956 9.867c0 1.576-1.3 2.873-2.886 2.873-1.587 0-2.887-1.296-2.887-2.873 0-1.576 1.3-2.873 2.887-2.873 1.587 0 2.886 1.297 2.886 2.873z"/></svg>',
    pinterest: '<svg viewBox="0 0 24 24" fill="#e60023"><path d="M12 0C5.373 0 0 5.372 0 12c0 5.084 3.163 9.426 7.627 11.174-.105-.949-.2-2.405.042-3.441.218-.937 1.407-5.965 1.407-5.965s-.359-.719-.359-1.782c0-1.668.967-2.914 2.171-2.914 1.023 0 1.518.769 1.518 1.69 0 1.029-.655 2.568-.994 3.995-.283 1.194.599 2.169 1.777 2.169 2.133 0 3.772-2.249 3.772-5.495 0-2.873-2.064-4.882-5.012-4.882-3.414 0-5.418 2.561-5.418 5.207 0 1.031.397 2.138.893 2.738.098.119.112.224.083.345l-.333 1.36c-.053.22-.174.267-.402.161-1.499-.698-2.436-2.889-2.436-4.649 0-3.785 2.75-7.262 7.929-7.262 4.163 0 7.398 2.967 7.398 6.931 0 4.136-2.607 7.464-6.227 7.464-1.216 0-2.359-.631-2.75-1.378l-.748 2.853c-.271 1.043-1.002 2.35-1.492 3.146C9.57 23.812 10.763 24 12 24c6.627 0 12-5.373 12-12 0-6.628-5.373-12-12-12z"/></svg>',
    snapchat: '<svg viewBox="0 0 24 24" fill="#fffc00"><path d="M12.206.793c.99 0 4.347.276 5.93 3.821.529 1.178.508 2.596.354 3.327-.096.456-.245 1.016-.288 1.272.303.225.618.418.975.568 2.678 1.147 3.565 1.705 3.705 2.532.113.657-.394 1.096-.852 1.163a2.3 2.3 0 0 1-.425.038c-.563-.019-1.188-.241-1.74-.6-.294-.207-.787-.676-1.186-1.033-.132.916-.383 2.078-1.037 2.963-.77 1.043-1.931 1.627-3.045 1.876-.946.21-2.238.319-3.738.262-.74-.029-1.598-.142-2.287-.395-.97-.357-1.74-.986-2.047-1.857-.229-.653-.346-1.893-.357-2.896-.371.416-.875.924-1.257 1.113-.562.281-1.158.443-1.645.443a1.44 1.44 0 0 1-.476-.073c-.535-.186-.725-.696-.551-1.245.29-.915 1.181-1.4 3.238-2.293.339-.139.62-.32.892-.522-.061-.297-.152-.711-.21-.994-.188-.893-.208-2.133.173-3.082C7.592 1.083 10.947.793 12.206.793z"/></svg>',
    threads: '<svg viewBox="0 0 24 24" fill="#ECE8E1"><path d="M16.634 10.092c-.291-.026-.585-.04-.882-.04-1.92 0-3.492.787-4.558 2.21l-.01-.01c-.138.191-.261.389-.37.594-1.066 1.873-1.26 3.697-.543 5.091.633 1.231 1.854 1.892 3.333 1.805 1.566-.093 2.944-.901 3.887-2.277.634-.926 1.05-2.106 1.236-3.506.34-.17.598-.389.752-.645.307-.507.29-1.1-.047-1.527-.336-.426-.932-.63-1.713-.587-.342.019-.707.068-1.085.143v-.25zm-1.397 6.08c-.635.926-1.526 1.425-2.487 1.482-.608.034-1.145-.199-1.482-.855-.355-.69-.326-1.872.374-3.102.116-.204.254-.397.407-.579.23-.271.51-.507.828-.697.098-.058.198-.112.301-.163.357-.165.742-.242 1.122-.214.647.046 1.122.359 1.307.868.124.338.078.747-.11 1.178-.03.07-.063.138-.097.205-.127.252-.294.473-.497.654-.067.059-.137.117-.21.173-.154.117-.325.211-.508.283-.493.196-.762.13-.908-.074-.077-.107-.08-.264-.005-.445l.488-1.184-1.36.571c-.947.398-1.726 1.038-2.194 1.862-.504.887-.617 1.869-.316 2.664.411 1.086 1.48 1.627 2.886 1.459 3.042-.362 5.456-4.232 5.684-7.87.055-.882.017-1.731-.109-2.51-.32-1.982-1.131-3.489-2.346-4.36-1.428-1.022-3.368-1.369-5.471-.976-3.302.616-5.897 3.058-6.756 6.349-.495 1.896-.302 3.793.543 5.346.798 1.467 2.127 2.438 3.76 2.744 1.389.26 2.888.106 4.184-.425l.237-.104-.628-1.394-.204.09c-1.867.82-3.885.728-5.02-.229-1.21-1.019-1.648-2.786-1.098-4.895.745-2.857 2.994-4.974 5.76-5.49 1.753-.328 3.343-.032 4.507.813 1.008.733 1.672 1.954 1.908 3.239.115.643.146 1.353.102 2.084-.198 3.286-2.445 6.788-4.935 7.085-.817.098-1.312-.17-1.523-.724-.294-.776.156-1.993 1.105-2.575l.06-.037z"/></svg>'
  };

  var PLATFORMS = [
    { key: "youtube", name: "YouTube", icon: PLATFORM_ICONS.youtube },
    { key: "instagram", name: "Instagram", icon: PLATFORM_ICONS.instagram },
    { key: "tiktok", name: "TikTok", icon: PLATFORM_ICONS.tiktok },
    { key: "twitter", name: "Twitter / X", icon: PLATFORM_ICONS.twitter },
    { key: "facebook", name: "Facebook", icon: PLATFORM_ICONS.facebook },
    { key: "reddit", name: "Reddit", icon: PLATFORM_ICONS.reddit },
    { key: "twitch", name: "Twitch", icon: PLATFORM_ICONS.twitch },
    { key: "vimeo", name: "Vimeo", icon: PLATFORM_ICONS.vimeo },
    { key: "soundcloud", name: "SoundCloud", icon: PLATFORM_ICONS.soundcloud },
    { key: "spotify", name: "Spotify", icon: PLATFORM_ICONS.spotify },
    { key: "bilibili", name: "Bilibili", icon: PLATFORM_ICONS.bilibili },
    { key: "dailymotion", name: "Dailymotion", icon: PLATFORM_ICONS.dailymotion },
    { key: "pinterest", name: "Pinterest", icon: PLATFORM_ICONS.pinterest },
    { key: "snapchat", name: "Snapchat", icon: PLATFORM_ICONS.snapchat },
    { key: "threads", name: "Threads", icon: PLATFORM_ICONS.threads }
  ];

  function platformByKey(key) {
    for (var i = 0; i < PLATFORMS.length; i++) {
      if (PLATFORMS[i].key === key) return PLATFORMS[i];
    }
    return null;
  }

  function $(id) { return document.getElementById(id); }

  /* ---------------------------------------------------------- filmstrip */
  function buildFilmstrip() {
    var track = $("filmstrip-track");
    if (!track) return;
    var one = PLATFORMS.map(function (p) {
      return '<div class="frame"><span class="frame__mark">' + p.icon +
        '</span><span class="frame__name">' + p.name + "</span></div>";
    }).join("");
    track.innerHTML =
      '<div class="filmstrip__set">' + one + "</div>" +
      '<div class="filmstrip__set" aria-hidden="true">' + one + "</div>";
  }

  /* ------------------------------------------------- academy leader ring */
  var RING_R = 88;
  var RING_C = 2 * Math.PI * RING_R;

  function buildLeader() {
    var ticks = $("leader-ticks");
    var fill = $("leader-ring-fill");
    if (!ticks || !fill) return;
    var out = "";
    for (var i = 0; i < 60; i++) {
      var major = i % 15 === 0;
      var a = (i * 6 - 90) * Math.PI / 180;
      var r1 = major ? 84 : 92;
      var r2 = 99;
      var x1 = (100 + r1 * Math.cos(a)).toFixed(2);
      var y1 = (100 + r1 * Math.sin(a)).toFixed(2);
      var x2 = (100 + r2 * Math.cos(a)).toFixed(2);
      var y2 = (100 + r2 * Math.sin(a)).toFixed(2);
      out += '<line class="leader__tick' + (major ? " leader__tick--major" : "") +
        '" x1="' + x1 + '" y1="' + y1 + '" x2="' + x2 + '" y2="' + y2 + '"/>';
    }
    ticks.innerHTML = out;
    fill.style.strokeDasharray = RING_C.toFixed(2);
    fill.style.strokeDashoffset = RING_C.toFixed(2);
    fill.setAttribute("transform", "rotate(-90 100 100)");
  }

  function setProgress(pct) {
    pct = Math.max(0, Math.min(100, Number(pct) || 0));
    var fill = $("leader-ring-fill");
    var hand = $("leader-hand");
    var pctEl = $("leader-pct");
    var pctText = Math.round(pct);
    if (fill) fill.style.strokeDashoffset = (RING_C * (1 - pct / 100)).toFixed(2);
    if (hand) hand.setAttribute("transform", "rotate(" + (pct * 3.6).toFixed(1) + " 100 100)");
    if (pctEl) pctEl.textContent = (pctText < 10 ? "0" : "") + pctText;
  }

  /* --------------------------------------------------------------- theme */
  var THEME_KEY = "zingy-theme";

  function applyTheme(theme) {
    var resolved = theme;
    if (theme === "auto") {
      resolved = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }
    document.documentElement.setAttribute("data-theme", resolved);
    try { localStorage.setItem(THEME_KEY, theme); } catch (e) { /* ignore */ }
    var buttons = document.querySelectorAll(".theme-switch button");
    for (var i = 0; i < buttons.length; i++) {
      buttons[i].setAttribute("aria-pressed", buttons[i].getAttribute("data-theme") === theme ? "true" : "false");
    }
  }

  function initTheme() {
    var stored = "dark";
    try { stored = localStorage.getItem(THEME_KEY) || "dark"; } catch (e) { stored = "dark"; }
    applyTheme(stored);
    var buttons = document.querySelectorAll(".theme-switch button");
    for (var i = 0; i < buttons.length; i++) {
      buttons[i].addEventListener("click", function () {
        applyTheme(this.getAttribute("data-theme"));
      });
    }
    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", function () {
      var current;
      try { current = localStorage.getItem(THEME_KEY); } catch (e) { current = null; }
      if (current === "auto") applyTheme("auto");
    });
  }

  /* -------------------------------------------------------------- status */
  function showStatus(message, kind) {
    var el = $("status");
    if (!el) return;
    el.textContent = message;
    el.className = "status" + (kind ? " status--" + kind : "");
    el.hidden = false;
  }

  function hideStatus() {
    var el = $("status");
    if (!el) return;
    el.hidden = true;
    el.textContent = "";
  }

  /* ------------------------------------------------------------ helpers */
  function formatDuration(totalSeconds) {
    var s = Math.round(Number(totalSeconds) || 0);
    if (s <= 0) return "--:--";
    var h = Math.floor(s / 3600);
    var m = Math.floor((s % 3600) / 60);
    var sec = s % 60;
    function pad(n) { return (n < 10 ? "0" : "") + n; }
    return (h > 0 ? pad(h) + ":" : "") + pad(m) + ":" + pad(sec);
  }

  /* --------------------------------------------------------------- fetch */
  var currentUrl = "";
  var currentDownloadId = null;

  function setSkeleton(on) {
    var sk = $("skeleton");
    if (sk) sk.hidden = !on;
  }

  async function fetchFormats() {
    var input = $("url-input");
    var url = input ? input.value.trim() : "";
    if (!url) {
      showStatus("Enter a video URL first.", "error");
      return;
    }
    currentUrl = url;

    var fetchBtn = $("fetch-btn");
    fetchBtn.disabled = true;
    fetchBtn.textContent = "Fetching";
    hideStatus();
    $("video-info").hidden = true;
    setSkeleton(true);

    try {
      var response = await fetch("/api/formats", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url: url })
      });
      var data = await response.json();

      if (!data.success) {
        showStatus(data.error || "That source could not be read.", "error");
        return;
      }

      var badge = platformByKey(data.platform);
      var thumb = $("video-thumbnail");
      var frame = $("slate-frame");
      if (data.thumbnail) {
        thumb.src = data.thumbnail;
        thumb.hidden = false;
        frame.classList.remove("is-empty");
      } else {
        thumb.removeAttribute("src");
        thumb.hidden = true;
        frame.classList.add("is-empty");
      }

      $("video-title").textContent = data.title || "Untitled";
      $("video-duration").textContent = formatDuration(data.duration);

      var badgeEl = $("platform-badge");
      badgeEl.innerHTML = (badge ? '<span class="badge">' + badge.icon + "</span>" : "") +
        "<span>" + (badge ? badge.name : (data.platform || "unknown")) + "</span>";

      var select = $("format-select");
      select.innerHTML = "";
      (data.formats || []).forEach(function (format) {
        var option = document.createElement("option");
        option.value = format.id;
        option.textContent = format.label;
        select.appendChild(option);
      });
      $("format-count").textContent = (data.formats || []).length;
      $("video-info").hidden = false;
    } catch (error) {
      showStatus("Could not reach the server: " + error.message, "error");
    } finally {
      setSkeleton(false);
      fetchBtn.disabled = false;
      fetchBtn.textContent = "Fetch";
    }
  }

  /* ------------------------------------------------------------ download */
  function setLeader(on) {
    $("progress-panel").hidden = !on;
    if (on) {
      setProgress(0);
      $("progress-speed").textContent = "--";
      $("progress-eta").textContent = "--";
      $("progress-state").textContent = "starting";
    }
  }

  async function startDownload() {
    var url = currentUrl || ($("url-input") ? $("url-input").value.trim() : "");
    if (!url) {
      showStatus("Fetch a video before starting a download.", "error");
      return;
    }
    var format = $("format-select").value;
    var btn = $("download-btn");
    btn.disabled = true;
    btn.textContent = "Starting";
    hideStatus();

    try {
      var response = await fetch("/api/download", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url: url, format: format })
      });
      var data = await response.json();

      if (!data.success) {
        showStatus(data.error || "The download could not be started.", "error");
        btn.disabled = false;
        btn.textContent = "Download";
        return;
      }
      currentDownloadId = data.download_id;
      setLeader(true);
      showStatus("Transfer running. The counter tracks real progress.", "success");
      pollProgress();
    } catch (error) {
      showStatus("Could not reach the server: " + error.message, "error");
      btn.disabled = false;
      btn.textContent = "Download";
    }
  }

  async function pollProgress() {
    if (!currentDownloadId) return;
    try {
      var response = await fetch("/api/progress/" + currentDownloadId);
      var data = await response.json();
      if (!data.success) {
        showStatus(data.error || "Lost track of that download.", "error");
        resetDownloadUI();
        return;
      }

      setProgress(data.progress);
      $("progress-speed").textContent = data.speed || "--";
      $("progress-eta").textContent = data.eta || "--";
      $("progress-state").textContent = data.status;

      if (data.status === "completed") {
        showStatus("Transfer complete. Saving the file to your device.", "success");
        triggerFileDownload(currentDownloadId);
        return;
      }
      if (data.status === "error") {
        showStatus(data.error || "The download failed.", "error");
        resetDownloadUI();
        return;
      }
      setTimeout(pollProgress, 500);
    } catch (error) {
      setTimeout(pollProgress, 1000);
    }
  }

  function triggerFileDownload(downloadId) {
    var a = document.createElement("a");
    a.href = "/api/file/" + downloadId;
    a.download = "";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setProgress(100);
    $("progress-state").textContent = "saved";
    resetDownloadUI();
    showStatus("Saved. Check your downloads.", "success");
  }

  function resetDownloadUI() {
    currentDownloadId = null;
    var btn = $("download-btn");
    btn.disabled = false;
    btn.textContent = "Download";
  }

  /* ---------------------------------------------------------------- init */
  function init() {
    buildFilmstrip();
    buildLeader();
    initTheme();

    var fetchBtn = $("fetch-btn");
    var downloadBtn = $("download-btn");
    var input = $("url-input");
    if (fetchBtn) fetchBtn.addEventListener("click", fetchFormats);
    if (downloadBtn) downloadBtn.addEventListener("click", startDownload);
    if (input) {
      input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") fetchFormats();
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
