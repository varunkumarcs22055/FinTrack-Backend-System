document.addEventListener('DOMContentLoaded', () => {
    // Requires any auth role
    if (!checkAuth(['ADMIN', 'ANALYST', 'VIEWER'])) return;
    setupNav();
    loadDashboard();
});

async function loadDashboard() {
    try {
        await Promise.all([
            fetchSummary(),
            fetchCategoryTotals(),
            fetchRecent()
        ]);
        // Charts library could go here for trends, but keeping it simple for assessment output.
    } catch (error) {
        console.error('Dashboard load error:', error);
    }
}

async function fetchSummary() {
    const summary = await apiFetch('/dashboard/summary');
    if (!summary) return;

    document.getElementById('total-income').textContent = `$${summary.totalIncome.toFixed(2)}`;
    document.getElementById('total-expenses').textContent = `$${summary.totalExpenses.toFixed(2)}`;
    document.getElementById('net-balance').textContent = `$${summary.netBalance.toFixed(2)}`;
    document.getElementById('total-records').textContent = summary.totalRecords;
}

async function fetchCategoryTotals() {
    const data = await apiFetch('/dashboard/category-totals');
    if (!data) return;

    renderCategoryList('income-categories', data.income);
    renderCategoryList('expense-categories', data.expense);
}

function renderCategoryList(elementId, categories) {
    const ul = document.getElementById(elementId);
    ul.innerHTML = '';
    
    if (!categories || categories.length === 0) {
        ul.innerHTML = '<li>No data available</li>';
        return;
    }
    
    categories.forEach(cat => {
        const li = document.createElement('li');
        li.className = 'flex justify-between items-center py-2 border-b last:border-0';
        li.innerHTML = `
            <span>${cat.category}</span>
            <span class="font-semibold">$${cat.total.toFixed(2)}</span>
        `;
        ul.appendChild(li);
    });
}

async function fetchRecent() {
    const records = await apiFetch('/dashboard/recent');
    const tbody = document.getElementById('recent-transactions');
    tbody.innerHTML = '';
    
    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="py-4 text-center text-gray-500">No recent transactions</td></tr>';
        return;
    }
    
    records.forEach(r => {
        const isIncome = r.type === 'INCOME';
        const color = isIncome ? 'text-green-600' : 'text-red-600';
        const prefix = isIncome ? '+' : '-';
        
        const tr = document.createElement('tr');
        tr.className = 'border-b hover:bg-gray-50';
        tr.innerHTML = `
            <td class="py-2 px-4">${new Date(r.date).toLocaleDateString()}</td>
            <td class="py-2 px-4">${r.category}</td>
            <td class="py-2 px-4">${r.description || '-'}</td>
            <td class="py-2 px-4 font-semibold ${color}">${prefix}$${r.amount.toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
    });
}
