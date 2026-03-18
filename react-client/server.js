import express from 'express';
import net from 'net';
import cors from 'cors';

const app = express();
const PORT = 3000;
const JAVA_SERVER_PORT = 8080;
const JAVA_SERVER_HOST = '127.0.0.1';

app.use(cors());
app.use(express.json());

// Helper function to communicate with Java TCP Server
function sendCommandToJava(command) {
    return new Promise((resolve, reject) => {
        const client = new net.Socket();
        
        client.connect(JAVA_SERVER_PORT, JAVA_SERVER_HOST, () => {
            console.log(`[Proxy] Sending to Java: ${command}`);
            client.write(command + '\n');
        });

        let data = '';
        client.on('data', (chunk) => {
            data += chunk.toString();
            try {
                // Java server returns single JSON lines per response
                const parsed = JSON.parse(data.trim());
                client.destroy();
                resolve(parsed);
            } catch (e) {
                // Wait for more data to arrive if JSON is incomplete
            }
        });

        client.on('error', (err) => {
            console.error(`[Proxy] Connection Error: ${err.message}`);
            reject(err);
        });

        client.on('close', () => {
            console.log('[Proxy] Connection wrapped up.');
        });
    });
}

// API Routes that map to Java Commands

// Get all students
app.get('/api/students', async (req, res) => {
    try {
        const result = await sendCommandToJava('GET_ALL');
        res.json(result);
    } catch (error) {
        res.status(500).json({ status: 'ERROR', message: "Failed to connect to back-end server." });
    }
});

// Add a new student
app.post('/api/students', async (req, res) => {
    const { name, sex, age, department } = req.body;
    if (!name || !sex || !age || !department) {
        return res.status(400).json({ status: 'ERROR', message: "Missing required fields" });
    }
    
    try {
        // Format: ADD <Name>;<Sex>;<Age>;<Department>
        const command = `ADD ${name};${sex};${age};${department}`;
        const result = await sendCommandToJava(command);
        res.json(result);
    } catch (error) {
        res.status(500).json({ status: 'ERROR', message: "Failed to connect to back-end server." });
    }
});

// Delete a student
app.delete('/api/students/:id', async (req, res) => {
    const { id } = req.params;
    try {
        const command = `DELETE ${id}`;
        const result = await sendCommandToJava(command);
        res.json(result);
    } catch (error) {
        res.status(500).json({ status: 'ERROR', message: "Failed to connect to back-end server." });
    }
});

// Search students
app.get('/api/students/search', async (req, res) => {
    const { field, value } = req.query;
    if (!field || !value) {
        return res.status(400).json({ status: 'ERROR', message: "Missing search parameters" });
    }
    try {
        const command = `SEARCH ${field} ${value}`;
        const result = await sendCommandToJava(command);
        res.json(result);
    } catch (error) {
        res.status(500).json({ status: 'ERROR', message: "Failed to connect to back-end server." });
    }
});

app.listen(PORT, () => {
    console.log(`Node Proxy Server listening on http://localhost:${PORT}`);
    console.log(`Forwarding requests via TCP to Java Server on ${JAVA_SERVER_HOST}:${JAVA_SERVER_PORT}`);
});
