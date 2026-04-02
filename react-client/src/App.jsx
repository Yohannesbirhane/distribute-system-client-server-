import { useState, useEffect } from 'react';
import './App.css';

const API_URL = 'http://localhost:3000/api/students';

function App() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const departments = [
    "Computer Science",
    "Mechanical Engineering",
    "Electrical Engineering",
    "Information Systems",
    "Software Engineering",
    "Civil Engineering",
    "Physics",
    "Journalism",
    "Architecture",
    "Mathematics"
  ];

  // Form State
  const [formData, setFormData] = useState({ name: '', sex: 'Male', age: '', department: departments[0] });
  
  // Search State
  const [searchData, setSearchData] = useState({ field: 'name', value: '' });

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const res = await fetch(API_URL);
      const data = await res.json();
      if (data.status === 'SUCCESS') setStudents(data.data || []);
      else setError(data.message);
    } catch (err) {
      setError('Could not connect to proxy server.');
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const showMessage = (msg, isError = false) => {
    if (isError) setError(msg);
    else setMessage(msg);
    setTimeout(() => { setMessage(null); setError(null); }, 5000);
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    const { name, age, sex, department } = formData;
    
    if (!name || !age || !department) {
      showMessage('Please fill all fields', true);
      return;
    }
    if (!/^[a-zA-Z\s]+$/.test(name)) {
      showMessage('Name must contain only letters and spaces.', true);
      return;
    }
    const ageNum = parseInt(age, 10);
    if (isNaN(ageNum) || ageNum < 15 || ageNum > 100) {
      showMessage('Age must be a number between 15 and 100.', true);
      return;
    }
    if (sex !== 'Male' && sex !== 'Female') {
      showMessage('Sex must be Male or Female.', true);
      return;
    }

    try {
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      const data = await res.json();
      if (data.status === 'SUCCESS') {
        showMessage(data.message);
        setFormData({ name: '', sex: 'Male', age: '', department: departments[0] });
        fetchStudents();
      } else showMessage(data.message, true);
    } catch (err) {
      showMessage('Error adding student.', true);
    }
  };

  const handleDelete = async (id) => {
    try {
      const res = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.status === 'SUCCESS') {
        showMessage(data.message);
        fetchStudents();
      } else showMessage(data.message, true);
    } catch (err) {
      showMessage('Error deleting student.', true);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchData.value) {
      fetchStudents();
      return;
    }
    try {
      const res = await fetch(`${API_URL}/search?field=${searchData.field}&value=${searchData.value}`);
      const data = await res.json();
      if (data.status === 'SUCCESS') {
        setStudents(data.data || []);
        showMessage(`Found ${data.data.length} results.`);
      } else showMessage(data.message, true);
    } catch (err) {
      showMessage('Error searching students.', true);
    }
  };

  return (
    <div className="container">
      <h1>University Student Manager</h1>
      
      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      <div className="controls">
        <div className="card">
          <h3>Add New Student</h3>
          <form onSubmit={handleAdd}>
            <input type="text" placeholder="Name" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
            <div className="flex-row">
              <select value={formData.sex} onChange={e => setFormData({...formData, sex: e.target.value})}>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
              </select>
              <input type="number" placeholder="Age" value={formData.age} onChange={e => setFormData({...formData, age: e.target.value})} />
            </div>
            <select value={formData.department} onChange={e => setFormData({...formData, department: e.target.value})}>
              {departments.map((dept, index) => (
                <option key={index} value={dept}>{dept}</option>
              ))}
            </select>
            <button type="submit">Add Student</button>
          </form>
        </div>

        <div className="card">
          <h3>Search Student</h3>
          <form onSubmit={handleSearch}>
            <select value={searchData.field} onChange={e => setSearchData({...searchData, field: e.target.value})}>
              <option value="name">Name</option>
              <option value="sex">Sex</option>
              <option value="age">Age</option>
              <option value="department">Department</option>
            </select>
            <input type="text" placeholder="Search value..." value={searchData.value} onChange={e => setSearchData({...searchData, value: e.target.value})} />
            <button type="submit">Search</button>
            <button type="button" onClick={() => { setSearchData({field: 'name', value: ''}); fetchStudents(); }} style={{backgroundColor: '#95a5a6'}}>Clear Search</button>
          </form>
        </div>
      </div>

      <div className="table-wrapper">
        <button onClick={fetchStudents} style={{marginBottom: '10px'}}>Refresh List</button>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Sex</th>
              <th>Age</th>
              <th>Department</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? <tr><td colSpan="6" style={{textAlign: 'center'}}>Loading...</td></tr> : null}
            {!loading && students.length === 0 ? <tr><td colSpan="6" style={{textAlign: 'center'}}>No students found.</td></tr> : null}
            {students.map(s => (
              <tr key={s.id}>
                <td>{s.id}</td>
                <td>{s.name}</td>
                <td>{s.sex}</td>
                <td>{s.age}</td>
                <td>{s.department}</td>
                <td>
                  <button className="danger" onClick={() => handleDelete(s.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default App;
