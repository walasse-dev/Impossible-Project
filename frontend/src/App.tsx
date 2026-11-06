import { BrowserRouter, Route, Routes } from 'react-router'
import HomePage from './pages/HomePage'
import ProductDetailPage from './pages/ProductDetailPage'

export default function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
        </Routes>
      </BrowserRouter>
  )
}