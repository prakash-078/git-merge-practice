const form = document.querySelector("#task-form");
const titleInput = document.querySelector("#task-title");
const taskList = document.querySelector("#task-list");
const taskCount = document.querySelector("#task-count");
const message = document.querySelector("#message");

async function loadTasks() {
  try {
    const response = await fetch("/api/tasks");
    if (!response.ok) throw new Error("Could not load tasks");
    const tasks = await response.json();
    renderTasks(tasks);
    message.textContent = "";
  } catch (error) {
    message.textContent = "Could not connect to the Java server. Is it running?";
  }
}

function renderTasks(tasks) {
  taskList.replaceChildren();
  tasks.forEach((task) => {
    const item = document.createElement("li");
    item.textContent = task.title;
    taskList.appendChild(item);
  });
  taskCount.textContent = `${tasks.length} task${tasks.length === 1 ? "" : "s"}`;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const title = titleInput.value.trim();
  if (!title) return;

  try {
    const response = await fetch("/api/tasks", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || "Could not add task");
    renderTasks(result);
    titleInput.value = "";
    message.textContent = "";
    titleInput.focus();
  } catch (error) {
    message.textContent = error.message || "Could not add task.";
  }
});

loadTasks();
