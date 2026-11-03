const API = '/api/v1/students';

// Load all students and redraw the table.
async function loadStudents() {
    const response = await fetch(API);
    const students = await response.json();
    const rows = document.getElementById('student-rows');
    rows.innerHTML = '';
    students.forEach(s => {
        const tr = document.createElement('tr');
        tr.setAttribute('data-testid', 'student-row');
        [s.id, s.name, s.age, s.gender, s.major, s.email ?? ''].forEach(value => {
            const td = document.createElement('td');
            td.textContent = value;
            tr.appendChild(td);
        });
        rows.appendChild(tr);
    });
    // Tests wait for this flag so they know the table is up to date.
    rows.setAttribute('data-loaded', 'true');
}

// Turn a ProblemDetail response into one readable line.
function problemToText(status, problem) {
    let text = status + ': ' + (problem.detail || problem.title || 'Request failed');
    if (problem.errors) {
        const fields = Object.entries(problem.errors).map(([field, msg]) => field + ' ' + msg);
        text += ' (' + fields.join(', ') + ')';
    }
    return text;
}

function showError(text) {
    const error = document.getElementById('error');
    error.textContent = text;
    error.hidden = false;
}

function clearError() {
    const error = document.getElementById('error');
    error.textContent = '';
    error.hidden = true;
}

document.getElementById('student-form').addEventListener('submit', async event => {
    event.preventDefault();
    clearError();
    const value = id => document.getElementById(id).value.trim();
    const body = {
        name: value('name'),
        age: value('age') === '' ? null : Number(value('age')),
        gender: value('gender'),
        major: value('major'),
        email: value('email') === '' ? null : value('email')
    };
    const response = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        showError(problemToText(response.status, problem));
        return;
    }
    document.getElementById('student-form').reset();
    await loadStudents();
});

loadStudents();
