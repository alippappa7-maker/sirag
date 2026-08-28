const { assertFails, assertSucceeds, initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const fs = require('fs');

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "siraj-test-project",
    firestore: {
      rules: fs.readFileSync("../firestore.rules", "utf8"),
    },
    storage: {
      rules: fs.readFileSync("../storage.rules", "utf8"),
    }
  });
});

after(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.clearStorage();
});

describe("Firestore Security Rules", () => {
  it("should allow a user to read their own user profile", async () => {
    const alice = testEnv.authenticatedContext("alice", { email: "alice@example.com" });
    await assertSucceeds(alice.firestore().collection("users").doc("alice").get());
  });

  it("should deny a user from reading someone else's profile", async () => {
    const alice = testEnv.authenticatedContext("alice", { email: "alice@example.com" });
    await assertFails(alice.firestore().collection("users").doc("bob").get());
  });

  it("should allow admin to read anyone's profile", async () => {
    const admin = testEnv.authenticatedContext("admin", { role: "ADMIN" });
    await assertSucceeds(admin.firestore().collection("users").doc("bob").get());
  });
  
  it("should prevent user from upgrading their own role", async () => {
    const alice = testEnv.authenticatedContext("alice");
    await assertFails(alice.firestore().collection("users").doc("alice").update({ role: "ADMIN" }));
  });
});
