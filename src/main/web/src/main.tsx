import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { routerFutureFlags } from './test/routerFuture'
import './styles.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter future={routerFutureFlags}>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)

