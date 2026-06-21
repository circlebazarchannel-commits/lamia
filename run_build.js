const { spawn } = require("child_process");
const fs = require("fs");

console.log("Starting gradle build in background...");
const out = fs.openSync("build_output_log.txt", "a");
const err = fs.openSync("build_output_log.txt", "a");

const child = spawn("gradle", [":app:assembleDebug"], {
  detached: true,
  stdio: ["ignore", out, err]
});

child.unref();
console.log("Build spawned in background. PID:", child.pid);
process.exit(0);
