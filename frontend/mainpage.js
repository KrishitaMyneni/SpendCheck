// SpendCheck Main Dashboard Logic

const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
const board = document.getElementById("weekgrid");

// In-memory expense cache by day: { "Monday": [ { id, title, amount, category, expenseDate } ] }
let expensesByDay = {};

function getCurrentWeekDates() {
  const now = new Date();
  const currentDay = now.getDay(); // 0 is Sunday, 1 is Monday...
  const diffToMonday = (currentDay === 0 ? -6 : 1) - currentDay;
  const monday = new Date(now);
  monday.setDate(now.getDate() + diffToMonday);

  const dates = {};
  days.forEach((day, index) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + index);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    dates[day] = `${yyyy}-${mm}-${dd}`;
  });
  return dates;
}

const currentWeekDates = getCurrentWeekDates();

function initUserUI() {
  const user = getUser();
  const greeting = document.getElementById("userGreeting");
  const logoutBtn = document.getElementById("logoutBtn");

  if (user && greeting) {
    greeting.textContent = `Hi, ${user.name || user.email}!`;
  }
  if (logoutBtn) {
    logoutBtn.style.display = "inline-block";
    logoutBtn.addEventListener("click", () => {
      SpendCheckAPI.logout();
    });
  }
}

function createDayColumn(day) {
  const col = document.createElement("div");
  col.className = "column";

  const title = document.createElement("h3");
  title.textContent = day;
  col.appendChild(title);

  const expenseList = document.createElement("div");
  expenseList.className = "expenses";
  expenseList.id = `${day}-expenses`;
  col.appendChild(expenseList);

  const formContainer = document.createElement("div");
  formContainer.style.display = "flex";
  formContainer.style.flexDirection = "column";
  formContainer.style.gap = "6px";
  formContainer.style.marginBottom = "10px";

  const amountInput = document.createElement("input");
  amountInput.type = "number";
  amountInput.step = "0.01";
  amountInput.min = "0.01";
  amountInput.placeholder = "₹ Amount";
  amountInput.style.height = "36px";
  amountInput.style.borderRadius = "6px";
  amountInput.style.border = "1px solid #ccc";
  amountInput.style.padding = "0 10px";

  const bottomRow = document.createElement("div");
  bottomRow.style.display = "flex";
  bottomRow.style.gap = "8px";
  bottomRow.style.alignItems = "center";

  const select = document.createElement("select");
  const categories = ["Travel", "Education", "Food", "Essentials", "Other"];
  categories.forEach(cat => {
    const option = document.createElement("option");
    option.value = cat;
    option.textContent = cat;
    select.appendChild(option);
  });
  select.style.height = "36px";
  select.style.borderRadius = "6px";
  select.style.border = "1px solid #ccc";
  select.style.padding = "0 10px";

  const btn = document.createElement("button");
  btn.textContent = "+";
  btn.style.backgroundColor = "rgb(18,16,139)";
  btn.style.color = "white";
  btn.style.border = "none";
  btn.style.padding = "6px 14px";
  btn.style.fontSize = "18px";
  btn.style.fontWeight = "bold";
  btn.style.borderRadius = "6px";
  btn.style.cursor = "pointer";
  btn.style.transition = "background-color 0.3s ease";
  btn.style.height = "36px";

  btn.addEventListener("click", async () => {
    const value = amountInput.value.trim();
    const category = select.value;

    if (!value || isNaN(value) || parseFloat(value) <= 0) {
      alert("Please enter a valid amount greater than 0");
      return;
    }

    btn.disabled = true;
    btn.textContent = "...";

    try {
      await SpendCheckAPI.createExpense({
        title: `${category} expense`,
        amount: parseFloat(value),
        category: category.toUpperCase(),
        expenseDate: currentWeekDates[day],
        notes: `Added on ${day}`
      });

      amountInput.value = "";
      await loadAndRenderExpenses();
    } catch (err) {
      alert(err.message || "Failed to add expense");
    } finally {
      btn.disabled = false;
      btn.textContent = "+";
    }
  });

  bottomRow.appendChild(select);
  bottomRow.appendChild(btn);

  formContainer.appendChild(amountInput);
  formContainer.appendChild(bottomRow);
  col.appendChild(formContainer);
  board.appendChild(col);
}

function renderDayExpenses(day) {
  const list = document.getElementById(`${day}-expenses`);
  if (!list) return;
  list.innerHTML = "";

  const items = expensesByDay[day] || [];

  items.forEach(exp => {
    const row = document.createElement("div");
    row.style.display = "flex";
    row.style.justifyContent = "space-between";
    row.style.alignItems = "center";
    row.style.backgroundColor = "#f3f3f3";
    row.style.padding = "6px 10px";
    row.style.borderRadius = "4px";
    row.style.margin = "4px 0";

    const p = document.createElement("span");
    const displayCat = exp.categoryDisplayName || exp.category;
    p.textContent = `₹${exp.amount} - ${displayCat}`;

    const delBtn = document.createElement("button");
    delBtn.textContent = "🗑";
    delBtn.title = "Delete expense";
    delBtn.style.border = "none";
    delBtn.style.background = "transparent";
    delBtn.style.cursor = "pointer";
    delBtn.style.fontSize = "16px";
    delBtn.style.color = "black";
    delBtn.style.transition = "transform 0.2s";

    delBtn.addEventListener("mouseover", () => {
      delBtn.style.transform = "scale(1.2)";
    });
    delBtn.addEventListener("mouseout", () => {
      delBtn.style.transform = "scale(1)";
    });

    delBtn.addEventListener("click", async () => {
      if (!confirm("Are you sure you want to delete this expense?")) return;
      try {
        await SpendCheckAPI.deleteExpense(exp.id);
        await loadAndRenderExpenses();
      } catch (err) {
        alert(err.message || "Failed to delete expense");
      }
    });

    row.appendChild(p);
    row.appendChild(delBtn);
    list.appendChild(row);
  });
}

async function loadAndRenderExpenses() {
  expensesByDay = {};
  days.forEach(day => {
    expensesByDay[day] = [];
  });

  try {
    const res = await SpendCheckAPI.getExpenses();
    const expenses = (res && res.data) || [];

    expenses.forEach(exp => {
      if (!exp.expenseDate) return;
      const d = new Date(exp.expenseDate + "T00:00:00");
      const weekdayIndex = (d.getDay() + 6) % 7; // 0=Monday ... 6=Sunday
      const weekday = days[weekdayIndex];
      if (weekday && expensesByDay[weekday]) {
        expensesByDay[weekday].push(exp);
      }
    });

    days.forEach(day => {
      renderDayExpenses(day);
    });
  } catch (err) {
    console.error("Error loading expenses:", err);
  }
}

// Initial bootstrap
if (requireAuth()) {
  initUserUI();
  board.innerHTML = "";
  days.forEach(day => {
    createDayColumn(day);
  });
  loadAndRenderExpenses();
}
