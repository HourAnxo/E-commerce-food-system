import { createContext, useContext, useEffect, useMemo, useState } from 'react'

// Cart lives client-side in localStorage. Each item stores a snapshot of the
// product plus a quantity. On checkout it is converted into a backend order.
const CartContext = createContext(null)

const STORAGE_KEY = 'foodapp.cart'

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : []
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  }, [items])

  const addItem = (product, quantity = 1) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.productId === product.productId)
      if (existing) {
        return prev.map((i) =>
          i.productId === product.productId
            ? { ...i, quantity: i.quantity + quantity }
            : i,
        )
      }
      return [
        ...prev,
        {
          productId: product.productId,
          productName: product.productName,
          price: Number(product.price),
          image_url: product.image_url,
          quantity,
        },
      ]
    })
  }

  const updateQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      removeItem(productId)
      return
    }
    setItems((prev) =>
      prev.map((i) => (i.productId === productId ? { ...i, quantity } : i)),
    )
  }

  const removeItem = (productId) => {
    setItems((prev) => prev.filter((i) => i.productId !== productId))
  }

  const clearCart = () => setItems([])

  const { totalItems, totalPrice } = useMemo(() => {
    return items.reduce(
      (acc, i) => {
        acc.totalItems += i.quantity
        acc.totalPrice += i.price * i.quantity
        return acc
      },
      { totalItems: 0, totalPrice: 0 },
    )
  }, [items])

  return (
    <CartContext.Provider
      value={{
        items,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
        totalItems,
        totalPrice,
      }}
    >
      {children}
    </CartContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCart() {
  return useContext(CartContext)
}
