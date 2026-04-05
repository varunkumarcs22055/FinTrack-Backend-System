document.addEventListener('DOMContentLoaded', () => {
    // Only Admin and Analyst can view this page
    if (!checkAuth(['ADMIN', 'ANALYST'])) return;
    
    setupNav();
    loadCategories();
    loadRecords();
    
    // Bind Filter form
    document.getElementById('filter-form').addEventListener('submit', (e) => {
        e.preventDefault();
        loadRecords();
    });
    
    // Bind Add button
    const userRole = getUser().role;
    const addBtn = document.getElementById('add-record-btn');
    
    if (userRole === 'ADMIN') {
        addBtn.addEventListener('click', () => showModal());
        document.getElementById('record-form').addEventListener('submit', handleSaveRecord);
        document.getElementById('cancel-btn').addEventListener('click', hideModal);
    } else {
        addBtn.style.display = 'none'; // Analysts can't add
    }
});

let currentRecordId = null;

async function loadCategories() {
    try {
        const categories = await apiFetch('/records/categories');
        const select = document.getElementById('filter-category');
        select.innerHTML = '<option value="">All Categories</option>';
        
        categories.forEach(cat => {
            const opt = document.createElement('option');
            opt.value = cat;
            opt.textContent = cat;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error('Failed to load categories');
    }
}

async function loadRecords() {
    try {
        const type = document.getElementById('filter-type').value;
        const category = document.getElementById('filter-category').value;
        const startDate = document.getElementById('filter-date-from').value;
        const endDate = document.getElementById('filter-date-to').value;
        
        // build query string
        const params = new URLSearchParams();
        if (type) params.append('type', type);
        if (category) params.append('category', category);
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        
        const pageResponse = await apiFetch(`/records?${params.toString()}`);
        renderRecords(pageResponse.content);
        
    } catch (e) {
        console.error('Failed to load records:', e);
    }
}

function renderRecords(records) {
    const tbody = document.getElementById('records-tbody');
    tbody.innerHTML = '';
    
    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-gray-500 py-4">No records found</td></tr>';
        return;
    }
    
    const userRole = getUser().role;
    
    records.forEach(r => {
        const tr = document.createElement('tr');
        tr.className = 'border-b hover:bg-gray-50';
        
        const isIncome = r.type === 'INCOME';
        const amountClass = isIncome ? 'text-green-600 font-bold' : 'text-red-600 font-bold';
        
        let actions = '-';
        if (userRole === 'ADMIN') {
            actions = `
                <button onclick='editRecord(${JSON.stringify(r)})' class="text-blue-500 hover:underline mr-2">Edit</button>
                <button onclick='deleteRecord(${r.id})' class="text-red-500 hover:underline">Delete</button>
            `;
        }
        
        tr.innerHTML = `
            <td class="p-3">${r.date}</td>
            <td class="p-3">
                <span class="px-2 py-1 text-xs rounded text-white ${isIncome ? 'bg-green-500' : 'bg-red-500'}">${r.type}</span>
            </td>
            <td class="p-3">${r.category}</td>
            <td class="p-3">${r.description || ''}</td>
            <td class="p-3 ${amountClass}">${isIncome ? '+' : '-'}$${r.amount.toFixed(2)}</td>
            <td class="p-3 text-sm">${r.createdByUsername}</td>
            <td class="p-3">${actions}</td>
        `;
        
        tbody.appendChild(tr);
    });
}

function showModal(title = "Add Record") {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('record-modal').classList.remove('hidden');
}

function hideModal() {
    document.getElementById('record-modal').classList.add('hidden');
    document.getElementById('record-form').reset();
    currentRecordId = null;
}

function editRecord(record) {
    currentRecordId = record.id;
    document.getElementById('amount').value = record.amount;
    document.getElementById('type').value = record.type;
    document.getElementById('category').value = record.category;
    document.getElementById('date').value = record.date;
    document.getElementById('description').value = record.description || '';
    
    showModal("Edit Record");
}

async function handleSaveRecord(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData);
    
    try {
        if (currentRecordId) {
            await apiFetch(`/records/${currentRecordId}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        } else {
            await apiFetch('/records', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }
        
        hideModal();
        loadRecords();
        loadCategories(); // update filter list if new category added
        
    } catch (error) {
        alert(error.message);
    }
}

async function deleteRecord(id) {
    if (!confirm('Are you sure you want to delete this record?')) return;
    
    try {
        await apiFetch(`/records/${id}`, { method: 'DELETE' });
        loadRecords();
    } catch (e) {
        alert(e.message);
    }
}
