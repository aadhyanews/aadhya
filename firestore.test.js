const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-aadhyane-bf72ab1e-2564-469c-b018-dee96101060c";
const ADMIN_UID = "admin_user_1";
const ADMIN_EMAIL = "srisahasramedicals@gmail.com";
const USER_ALICE = "alice_uid";
const USER_BOB = "bob_uid";

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: can read published articles, but not drafts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("articles").doc("pub_1").set({
      titleTe: "తాజా వార్తలు 1",
      contentTe: "ముఖ్యమైన సమాచారం మరియు వివరాలు",
      categoryId: "latest",
      authorName: "రిపోర్టర్",
      status: "published",
      isBreaking: false,
      isFeatured: false,
      createdAt: new Date(),
      updatedAt: new Date()
    });
    await db.collection("articles").doc("draft_1").set({
      titleTe: "డ్రాఫ్ట్ వార్త",
      contentTe: "ఈ వార్త ఇంకా ప్రచురించబడలేదు",
      categoryId: "latest",
      authorName: "రిపోర్టర్",
      status: "draft",
      isBreaking: false,
      isFeatured: false,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("articles").doc("pub_1").get());
  await assertFails(unauthDb.collection("articles").doc("draft_1").get());
});

test("Unauthenticated user: cannot create an article", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("articles").doc("new_art").set({
      titleTe: "అనధికార వార్త",
      contentTe: "టెస్ట్ వార్త వివరణ",
      categoryId: "politics",
      authorName: "అజ్ఞాత",
      status: "published",
      isBreaking: false,
      isFeatured: false,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Admin user: can create published news article", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL }).firestore();
  await assertSucceeds(
    adminDb.collection("articles").doc("art_admin_1").set({
      titleTe: "హైదరాబాద్‌లో భారీ వర్షం",
      contentTe: "హైదరాబాద్ నగరంలో కుండపోత వర్షం కురుస్తోంది.",
      categoryId: "hyderabad",
      authorName: "ఆధ్య ప్రతినిధి",
      status: "published",
      isBreaking: true,
      isFeatured: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Regular user: cannot modify categories or create articles", async () => {
  const userDb = testEnv.authenticatedContext(USER_ALICE, { email: "alice@example.com" }).firestore();
  await assertFails(
    userDb.collection("categories").doc("tech").set({
      nameTe: "టెక్నాలజీ",
      slug: "technology",
      isActive: true,
      sortOrder: 1
    })
  );
});

test("User bookmarks: isolated to the user", async () => {
  const aliceDb = testEnv.authenticatedContext(USER_ALICE).firestore();
  const bobDb = testEnv.authenticatedContext(USER_BOB).firestore();

  // Alice can save her bookmark
  await assertSucceeds(
    aliceDb.collection("users").doc(USER_ALICE).collection("bookmarks").doc("art_1").set({
      articleId: "art_1",
      titleTe: "బుక్‌మార్క్ చేసిన వార్త"
    })
  );

  // Bob cannot read Alice's bookmark
  await assertFails(
    bobDb.collection("users").doc(USER_ALICE).collection("bookmarks").doc("art_1").get()
  );
});
