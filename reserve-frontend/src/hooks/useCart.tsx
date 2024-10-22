import {createContext, ReactNode, useCallback, useContext, useEffect, useState} from 'react'
import type {CartItem, Menu, Store} from '@customTypes/domain.d.ts'

type Cart = {
    store?: Store
    items: CartItem[]
}

type CartContext = {
    cart: Cart
    addItem: (store: Store, item: Menu, quantity: number) => void
    updateItem: (itemIndex: number, quantity: number) => void
    removeItem: (itemIndex: number) => void
    clear: () => void
}

const cartContext = createContext<CartContext>({} as CartContext)

function getCartFromLocalStorage(): Cart {
    const cart = localStorage.getItem('cart')
    return cart ? JSON.parse(cart) : {items: []}
}

function setCartToLocalStorage(cart: Cart) {
    localStorage.setItem('cart', JSON.stringify(cart))
}

export function CartProvider({children}: { children: ReactNode }) {
    const [cart, setCart] = useState<Cart>({items: []})

    useEffect(() => {
        setCart(getCartFromLocalStorage())
    }, [])

    // Update cart state and save to local storage
    const updateCart = useCallback((newCart: Cart) => {
        setCart(newCart)
        setCartToLocalStorage(newCart)
    }, [])

    const removeItem = useCallback((itemIndex: number) => {
        let newCart: Cart = {store: cart.store, items: cart.items.slice()}
        newCart.items.splice(itemIndex, 1)
        if (newCart.items.length === 0) newCart = {items: []}
        updateCart(newCart)
    }, [cart, updateCart])

    const addItem = useCallback((newStore: Store, menu: Menu, quantity: number) => {
        if (cart.store && cart.store.storeId !== newStore.storeId) {
            const newCartItems: CartItem[] = [{menuId: menu.menuId, name: menu.name, price: menu.price, quantity}]
            updateCart({store: newStore, items: newCartItems})
            return
        }

        const index = cart.items.findIndex(item => item.menuId === menu.menuId)

        if (index !== -1 && quantity === 0) {
            removeItem(index)
            return
        }

        const newCart = {store: newStore} as Cart
        if (index !== -1) {
            newCart.items = cart.items.map(item => item.menuId === menu.menuId ? {...item, quantity} : item)
        } else {
            newCart.items = [...cart.items, {menuId: menu.menuId, name: menu.name, price: menu.price, quantity}]
        }
        updateCart(newCart)
    }, [cart, removeItem, updateCart])

    const updateItem = useCallback((itemIndex: number, quantity: number) => {
        if (quantity === 0) {
            removeItem(itemIndex)
            return
        }
        const newCart = {
            store: cart.store,
            items: cart.items.map((item, index) => index === itemIndex ? {...item, quantity} : item),
        }
        setCart(newCart)
        updateCart(newCart)
    }, [cart, removeItem, updateCart])

    const clear = useCallback(() => {
        updateCart({items: []})
    }, [updateCart])

    return (
        <cartContext.Provider value={{cart, addItem, updateItem, removeItem, clear}}>
            {children}
        </cartContext.Provider>
    )
}

export function useCart() {
    const context = useContext(cartContext)
    if (!context) throw new Error('useCart must be used in CartContext.')
    return context
}
