import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { customerApi } from '../api/services'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
    const { login } = useAuth()
    const navigate = useNavigate()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState(null)
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError(null)
        setLoading(true)
        try {
            const res = await customerApi.login(email.trim(), password)
            login(res.data)
            navigate('/')
        } catch {
            setError('Invalid email or password.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth-card">
            <h1>Login</h1>
            <form onSubmit={handleSubmit}>
                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Your password"
                />

                {error && <p className="error-text">{error}</p>}

                <button type="submit" className="btn btn-primary btn-lg" disabled={loading}>
                    {loading ? 'Logging in…' : 'Login'}
                </button>
            </form>
            <p className="auth-switch">
                Don’t have an account? <Link to="/register">Register</Link>
            </p>
        </div>
    )
}