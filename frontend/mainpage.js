// SpendCheck Main Dashboard Logic

const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
const board = document.getElementById("weekgrid");

// In-memory expense cache by day: { "Monday": [ { id, title, amount, category, expenseDate } ] }
let expensesByDay = {};

// Universal Currency Formatter: Thousands separators & no decimals when whole (e.g. ₹52,568 and ₹200)
function formatCurrency(amount) {
  const num = parseFloat(amount);
  if (isNaN(num)) return "₹0";
  const rounded = Math.round(num * 100) / 100;
  const isWhole = (rounded % 1 === 0);
  return "₹" + rounded.toLocaleString("en-US", {
    minimumFractionDigits: isWhole ? 0 : 2,
    maximumFractionDigits: isWhole ? 0 : 2
  });
}

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

  // Check if this column is today
  const todayIdx = (new Date().getDay() + 6) % 7; // 0=Monday ... 6=Sunday
  const isToday = (days[todayIdx] === day);
  if (isToday) {
    col.classList.add("today-card");
  }

  // Header: Day Name + subtle "Today" badge + small daily total on the right
  const header = document.createElement("div");
  header.className = "day-card-header";

  const title = document.createElement("h3");
  title.className = "day-card-title";
  title.textContent = day;

  if (isToday) {
    const todayTag = document.createElement("span");
    todayTag.className = "today-tag";
    todayTag.textContent = "Today";
    title.appendChild(todayTag);
  }

  const dayTotalBadge = document.createElement("span");
  dayTotalBadge.className = "day-total-badge";
  dayTotalBadge.id = `${day}-total-badge`;
  dayTotalBadge.textContent = "₹0";

  header.appendChild(title);
  header.appendChild(dayTotalBadge);
  col.appendChild(header);

  // Expenses container (compact rows)
  const expenseList = document.createElement("div");
  expenseList.className = "expenses";
  expenseList.id = `${day}-expenses`;
  col.appendChild(expenseList);

  // Tidy inline per-day add form
  const formContainer = document.createElement("div");
  formContainer.className = "form-container";

  const amountInput = document.createElement("input");
  amountInput.type = "number";
  amountInput.step = "0.01";
  amountInput.min = "0.01";
  amountInput.placeholder = "₹ Amount";
  amountInput.className = "day-amount-input";

  const bottomRow = document.createElement("div");
  bottomRow.className = "form-row";

  const select = document.createElement("select");
  select.className = "day-category-select";
  const categories = ["Travel", "Education", "Food", "Essentials", "Other"];
  categories.forEach(cat => {
    const option = document.createElement("option");
    option.value = cat;
    option.textContent = cat;
    select.appendChild(option);
  });

  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "btn-add-expense";
  btn.textContent = "Add";

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
      btn.textContent = "Add";
    }
  });

  bottomRow.appendChild(select);
  bottomRow.appendChild(btn);

  formContainer.appendChild(amountInput);
  formContainer.appendChild(bottomRow);
  col.appendChild(formContainer);

  if (board) {
    board.appendChild(col);
  }
}

function renderDayExpenses(day) {
  const list = document.getElementById(`${day}-expenses`);
  if (!list) return;
  list.innerHTML = "";

  const items = expensesByDay[day] || [];
  const dayTotal = items.reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0);

  // Update small daily total on the right
  const badge = document.getElementById(`${day}-total-badge`);
  if (badge) {
    badge.textContent = formatCurrency(dayTotal);
  }

  // Replace big empty box with light "Nothing logged" empty state
  if (items.length === 0) {
    const emptyEl = document.createElement("div");
    emptyEl.className = "day-empty-placeholder";
    emptyEl.textContent = "Nothing logged";
    list.appendChild(emptyEl);
    return;
  }

  items.forEach(exp => {
    const row = document.createElement("div");
    row.className = "expense-row-item";

    const left = document.createElement("div");
    left.className = "expense-row-left";
    const displayCat = exp.categoryDisplayName || exp.category || "Other";
    const catClass = "cat-badge-" + displayCat.toLowerCase();
    left.innerHTML = `<span class="expense-amount-text">${formatCurrency(exp.amount)}</span><span class="cat-badge ${catClass}">${displayCat}</span>`;

    // Clean inline SVG trash outline delete button
    const delBtn = document.createElement("button");
    delBtn.type = "button";
    delBtn.className = "btn-delete-expense";
    delBtn.title = "Delete expense";
    delBtn.setAttribute("aria-label", "Delete expense");
    delBtn.innerHTML = `
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 6h18"></path>
        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"></path>
        <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
      </svg>
    `;

    delBtn.addEventListener("click", async () => {
      if (!confirm("Are you sure you want to delete this expense?")) return;
      try {
        await SpendCheckAPI.deleteExpense(exp.id);
        await loadAndRenderExpenses();
      } catch (err) {
        alert(err.message || "Failed to delete expense");
      }
    });

    row.appendChild(left);
    row.appendChild(delBtn);
    list.appendChild(row);
  });
}

// Update Dashboard Summary Stats & Category Spending Chart (Runs ONCE per data load)
function updateDashboardMetricsAndChart() {
  let totalSpent = 0;
  let totalCount = 0;
  let activeDays = 0;

  const categoryTotals = {
    travel: { name: "Travel", amount: 0, count: 0, color: "var(--cat-travel-text)" },
    education: { name: "Education", amount: 0, count: 0, color: "var(--cat-education-text)" },
    food: { name: "Food", amount: 0, count: 0, color: "var(--cat-food-text)" },
    essentials: { name: "Essentials", amount: 0, count: 0, color: "var(--cat-essentials-text)" },
    other: { name: "Other", amount: 0, count: 0, color: "var(--cat-other-text)" }
  };

  days.forEach(day => {
    const items = expensesByDay[day] || [];
    const dayTotal = items.reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0);

    const badge = document.getElementById(`${day}-total-badge`);
    if (badge) {
      badge.textContent = formatCurrency(dayTotal);
    }

    if (items.length > 0) {
      activeDays++;
      items.forEach(exp => {
        totalCount++;
        const amt = parseFloat(exp.amount) || 0;
        totalSpent += amt;

        const catName = (exp.categoryDisplayName || exp.category || "Other").toLowerCase();
        if (categoryTotals[catName]) {
          categoryTotals[catName].amount += amt;
          categoryTotals[catName].count += 1;
        } else {
          categoryTotals.other.amount += amt;
          categoryTotals.other.count += 1;
        }
      });
    }
  });

  // Update Summary Metric Cards
  const summarySpentEl = document.getElementById("summaryTotalSpent");
  const summaryCountEl = document.getElementById("summaryTotalCount");
  const summaryDaysEl = document.getElementById("summaryActiveDays");

  if (summarySpentEl) summarySpentEl.textContent = formatCurrency(totalSpent);
  if (summaryCountEl) summaryCountEl.textContent = totalCount;
  if (summaryDaysEl) summaryDaysEl.textContent = `${activeDays} / 7`;

  // Update Category Spending Chart & Legend
  const chartContainer = document.getElementById("chartContent");
  const chartBadge = document.getElementById("chartTotalBadge");

  if (chartBadge) {
    chartBadge.textContent = formatCurrency(totalSpent);
  }

  if (!chartContainer) return;

  if (totalSpent <= 0) {
    chartContainer.innerHTML = `
      <div class="chart-empty-state">
        <div class="empty-icon-circle">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12c0 4.97-4.03 9-9 9s-9-4.03-9-9 4.03-9 9-9"></path>
            <path d="M12 7v5l3 3"></path>
          </svg>
        </div>
        <div class="empty-title">No expenses recorded yet</div>
        <div class="empty-desc">Log your daily expenses on any day card below to see your visual spending distribution and category share.</div>
      </div>
    `;
    return;
  }

  let barSegmentsHtml = '<div class="chart-stacked-bar">';
  let legendItemsHtml = '<div class="chart-legend-grid">';

  Object.keys(categoryTotals).forEach(key => {
    const cat = categoryTotals[key];
    const pct = totalSpent > 0 ? (cat.amount / totalSpent) * 100 : 0;

    if (pct > 0) {
      barSegmentsHtml += `
        <div class="chart-segment" style="width: ${pct.toFixed(2)}%; background-color: ${cat.color};" title="${cat.name}: ${formatCurrency(cat.amount)} (${pct.toFixed(1)}%)"></div>
      `;
    }

    legendItemsHtml += `
      <div class="chart-legend-item">
        <div class="chart-legend-left">
          <span class="chart-legend-dot" style="background-color: ${cat.color};"></span>
          <span class="chart-legend-cat">${cat.name}</span>
        </div>
        <div class="chart-legend-right">
          <span class="chart-legend-amount">${formatCurrency(cat.amount)}</span>
          <span class="chart-legend-pct">${pct.toFixed(1)}% (${cat.count} item${cat.count === 1 ? '' : 's'})</span>
        </div>
      </div>
    `;
  });

  barSegmentsHtml += '</div>';
  legendItemsHtml += '</div>';

  chartContainer.innerHTML = barSegmentsHtml + legendItemsHtml;
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

    // Run formatting, metrics, and chart updates ONCE per data load
    updateDashboardMetricsAndChart();
  } catch (err) {
    console.error("Error loading expenses:", err);
  }
}

// Initial bootstrap
if (requireAuth()) {
  initUserUI();
  if (board) {
    board.innerHTML = "";
    days.forEach(day => {
      createDayColumn(day);
    });
  }
  loadAndRenderExpenses();
}
