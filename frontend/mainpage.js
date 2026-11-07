const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
const board = document.getElementById("weekgrid");

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

  
  btn.addEventListener("click", () => {
    const value = amountInput.value.trim();
    const category = select.value;

    if (value && !isNaN(value)) {
      const stored = JSON.parse(localStorage.getItem(day)) || [];
      stored.push({ amount: value, category: category });
      localStorage.setItem(day, JSON.stringify(stored));
      amountInput.value = "";
      renderExpenses(day);
    }
  });

  bottomRow.appendChild(select);
  bottomRow.appendChild(btn);

  
  formContainer.appendChild(amountInput);
  formContainer.appendChild(bottomRow);
  col.appendChild(formContainer);
  board.appendChild(col);
}

function renderExpenses(day) {
  const list = document.getElementById(`${day}-expenses`);
  list.innerHTML = "";
  const expenses = JSON.parse(localStorage.getItem(day)) || [];

  expenses.forEach((exp, index) => {
    const row = document.createElement("div");
    row.style.display = "flex";
    row.style.justifyContent = "space-between";
    row.style.alignItems = "center";
    row.style.backgroundColor = "#f3f3f3";
    row.style.padding = "6px 10px";
    row.style.borderRadius = "4px";
    row.style.margin = "4px 0";

    const p = document.createElement("span");
    p.textContent = `₹${exp.amount} - ${exp.category}`;

    const delBtn = document.createElement("button");
    delBtn.textContent = "🗑";
    delBtn.style.border = "none";
    delBtn.style.background = "transparent";
    delBtn.style.cursor = "pointer";
    delBtn.style.fontSize = "18px";
    delBtn.style.color = "black";
    delBtn.style.transition = "transform 0.2s";

    delBtn.addEventListener("mouseover", () => {
      delBtn.style.transform = "scale(1.2)";
    });
    delBtn.addEventListener("mouseout", () => {
      delBtn.style.transform = "scale(1)";
    });

    delBtn.addEventListener("click", () => {
      expenses.splice(index, 1);
      localStorage.setItem(day, JSON.stringify(expenses));
      renderExpenses(day);
    });

    row.appendChild(p);
    row.appendChild(delBtn);
    list.appendChild(row);
  });
}


days.forEach(day => {
  createDayColumn(day);
  renderExpenses(day);
});