const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

function findServiceAccountPath() {
  const argPath = process.argv[2];
  if (argPath) {
    const resolved = path.resolve(argPath);
    if (!fs.existsSync(resolved)) {
      throw new Error(`Không tìm thấy file key tại: ${resolved}`);
    }
    return resolved;
  }

  const searchDirs = [__dirname, path.resolve(__dirname, "../..")];
  for (const dir of searchDirs) {
    const explicit = path.join(dir, "serviceAccountKey.json");
    if (fs.existsSync(explicit)) return explicit;

    const files = fs.readdirSync(dir);
    const adminSdkFile = files.find(
      (f) => f.endsWith(".json") && f.includes("firebase-adminsdk")
    );
    if (adminSdkFile) return path.join(dir, adminSdkFile);
  }

  throw new Error(
    "Chưa tìm thấy file Service Account JSON.\n" +
      "Hãy copy file *firebase-adminsdk*.json (hoặc đổi tên thành serviceAccountKey.json) " +
      `vào thư mục ${__dirname}, hoặc chạy: node seed.js "<đường_dẫn_file.json>"`
  );
}

async function seed() {
  const keyPath = findServiceAccountPath();
  const serviceAccount = JSON.parse(fs.readFileSync(keyPath, "utf8"));
  if (serviceAccount.type !== "service_account") {
    throw new Error(
      `File ${keyPath} không phải Service Account Key (type != "service_account").`
    );
  }

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });

  const db = admin.firestore();
  const now = admin.firestore.Timestamp.now();
  const oneMonthLater = admin.firestore.Timestamp.fromMillis(
    Date.now() + 30 * 24 * 60 * 60 * 1000
  );

  const batch = db.batch();

  // 1. Collection: users
  batch.set(db.collection("users").doc("user_demo_01"), {
    uid: "user_demo_01",
    displayName: "Nguyễn Văn A",
    email: "demo@grandsoniq.app",
    avatarUrl: "",
    favoriteSongIds: ["song_001"],
    recentSongIds: ["song_001"],
    followingArtistIds: ["art_001"],
    followingUserIds: [],
    savedAlbumIds: ["alb_001"],
    isPremium: false,
    premiumUntil: null,
    settings: {
      audioQuality: "high",
      wifiOnlyDownload: true,
      dataSaver: false,
    },
    createdAt: now,
  });

  // 2. Collection: artists
  batch.set(db.collection("artists").doc("art_001"), {
    id: "art_001",
    name: "Nghệ sĩ Mẫu",
    nameLowercase: "nghe si mau",
    avatarUrl: "",
    coverUrl: "",
    bio: "Hồ sơ nghệ sĩ mẫu khởi tạo cấu trúc Firestore.",
    followerCount: 0,
    isVerified: true,
  });

  // 3. Collection: albums
  batch.set(db.collection("albums").doc("alb_001"), {
    id: "alb_001",
    title: "Album Mẫu",
    titleLowercase: "album mau",
    artistId: "art_001",
    artistName: "Nghệ sĩ Mẫu",
    coverUrl: "",
    songIds: ["song_001"],
    releaseDate: now,
  });

  // 4. Collection: songs
  batch.set(db.collection("songs").doc("song_001"), {
    id: "song_001",
    title: "Bài Hát Mẫu",
    titleLowercase: "bai hat mau",
    artistId: "art_001",
    artistName: "Nghệ sĩ Mẫu",
    albumId: "alb_001",
    genre: "V-Pop",
    mood: "Chill",
    audioUrl: "",
    audioUrls: {
      normal: "",
      high: "",
      lossless: "",
    },
    coverUrl: "",
    mvUrl: "",
    shortVideoUrl: "",
    duration: 210000,
    playCount: 0,
    lyrics: "[00:00.00] Lời bài hát mẫu",
    isPremium: false,
    createdAt: now,
  });

  // 5. Collection: playlists
  batch.set(db.collection("playlists").doc("pl_001"), {
    id: "pl_001",
    name: "Nhạc chill code đêm",
    description: "Playlist mẫu khởi tạo collection playlists.",
    ownerId: "system",
    ownerName: "GrandSoniq",
    coverUrl: "",
    category: "mood",
    songIds: ["song_001"],
    isPublic: true,
    isCollaborative: false,
    collaboratorIds: [],
    createdAt: now,
  });

  // 6. Collection: comments
  batch.set(db.collection("comments").doc("cmt_001"), {
    id: "cmt_001",
    targetId: "song_001",
    targetType: "song",
    userId: "user_demo_01",
    userName: "Nguyễn Văn A",
    userAvatar: "",
    content: "Bình luận mẫu theo nhịp nhạc.",
    songPositionMs: 15000,
    createdAt: now,
  });

  // 7. Collection: listeningRooms
  batch.set(db.collection("listeningRooms").doc("JAM889"), {
    id: "JAM889",
    hostId: "user_demo_01",
    roomName: "Phòng nhạc cuối tuần",
    isActive: true,
    memberIds: ["user_demo_01"],
    currentSongId: "song_001",
    isPlaying: false,
    positionMs: 0,
    queueSongIds: ["song_001"],
    updatedAt: now,
  });

  // 8. Collection: subscriptions
  batch.set(db.collection("subscriptions").doc("sub_001"), {
    id: "sub_001",
    userId: "user_demo_01",
    planType: "monthly",
    amount: 59000,
    currency: "VND",
    provider: "google_play",
    status: "active",
    startDate: now,
    endDate: oneMonthLater,
  });

  await batch.commit();
  console.log(
    "Đã tạo thành công 8 collections trên Firestore: users, artists, albums, songs, playlists, comments, listeningRooms, subscriptions."
  );
  process.exit(0);
}

seed().catch((err) => {
  console.error("Lỗi khi seed Firestore:", err.message || err);
  process.exit(1);
});
