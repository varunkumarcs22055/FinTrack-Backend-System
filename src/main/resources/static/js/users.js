document.addEventListener('DOMContentLoaded', () => {
    // Only Admin can view user management
    if (!checkAuth(['ADMIN'])) return;
    
    setupNav();
    loadUsers();
});

async function loadUsers() {
    try {
        const users = await apiFetch('/users');
        const tbody = document.getElementById('users-tbody');
        tbody.innerHTML = '';
        
        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="p-4 text-center">No users found</td></tr>';
            return;
        }
        
        users.forEach(u => {
            const tr = document.createElement('tr');
            tr.className = 'border-b hover:bg-gray-50';
            
            const activeStatus = u.active ? 
                '<span class="px-2 py-1 text-xs rounded bg-green-100 text-green-800">Active</span>' : 
                '<span class="px-2 py-1 text-xs rounded bg-red-100 text-red-800">Inactive</span>';
            
            const actionToggle = u.active ? 
                `<button onclick="toggleUser(${u.id})" class="text-orange-500 hover:underline mr-3">Disable</button>` : 
                `<button onclick="toggleUser(${u.id})" class="text-green-500 hover:underline mr-3">Enable</button>`;
                
            // Avoid disabling the admin you are currently logged in as
            const isSelf = u.username === getUser().username;
            const actions = isSelf ? 
                '<span class="text-gray-400 italic">Current User</span>' : 
                `
                ${actionToggle}
                <button onclick="deleteUser(${u.id}, '${u.username}')" class="text-red-500 hover:underline">Delete</button>
                `;
            
            // Dropdown to edit role inline
            const roleSelect = isSelf ? 
                `<span class="font-bold p-1">${u.role}</span>` :
                `
                <select class="border rounded p-1" onchange="updateRole(${u.id}, this.value)">
                    <option value="ADMIN" ${u.role==='ADMIN'?'selected':''}>ADMIN</option>
                    <option value="ANALYST" ${u.role==='ANALYST'?'selected':''}>ANALYST</option>
                    <option value="VIEWER" ${u.role==='VIEWER'?'selected':''}>VIEWER</option>
                </select>
                `;
                
            tr.innerHTML = `
                <td class="p-3">${u.id}</td>
                <td class="p-3 font-semibold">${u.username}</td>
                <td class="p-3 text-gray-600">${u.email}</td>
                <td class="p-3">${roleSelect}</td>
                <td class="p-3">${activeStatus}</td>
                <td class="p-3">${actions}</td>
            `;
            
            tbody.appendChild(tr);
        });
        
    } catch (e) {
        console.error('Failed to load users:', e);
    }
}

async function updateRole(userId, newRole) {
    try {
        await apiFetch(`/users/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ role: newRole })
        });
        loadUsers(); // Refresh
        alert('Role updated successfully.');
    } catch (e) {
        alert(e.message);
        loadUsers(); // Revert back dropdown
    }
}

async function toggleUser(userId) {
    try {
        await apiFetch(`/users/${userId}/status`, { method: 'PUT' });
        loadUsers();
    } catch (e) {
        alert(e.message);
    }
}

async function deleteUser(userId, username) {
    if (!confirm(`Are you absolutely sure you want to delete user '${username}'?`)) return;
    
    try {
        await apiFetch(`/users/${userId}`, { method: 'DELETE' });
        loadUsers();
    } catch (e) {
        alert(e.message);
    }
}
