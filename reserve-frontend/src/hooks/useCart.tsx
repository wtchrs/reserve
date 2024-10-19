import {createContext, ReactNode, useCallback, useContext, useState} from 'react'
import {CartItem, Menu, Store} from '../type.ts'

type CartContext = {
    store?: Store
    cartItems: CartItem[]
    addItem: (store: Store, item: Menu, quantity: number) => void
    updateItem: (itemIndex: number, quantity: number) => void
    removeItem: (itemIndex: number) => void
    clear: () => void
}

const cartContext = createContext<CartContext>({} as CartContext)

export function CartProvider({children}: { children: ReactNode }) {
    const [store, setStore] = useState<Store>()
    const [cartItems, setCartItems] = useState<CartItem[]>([])

    const removeItem = useCallback((itemIndex: number) => {
        const newCartItems = cartItems.slice()
        newCartItems.splice(itemIndex, 1)
        setCartItems(newCartItems)
        if (newCartItems.length === 0) setStore(undefined)
    }, [cartItems])

    const addItem = useCallback((newStore: Store, menu: Menu, quantity: number) => {
        if (store && store.storeId !== newStore.storeId) {
            setStore(newStore)
            setCartItems([{menuId: menu.menuId, name: menu.name, price: menu.price, quantity}])
            return
        }

        setStore(newStore)
        const index = cartItems.findIndex(item => item.menuId === menu.menuId)

        if (index !== -1 && quantity === 0) {
            removeItem(index)
            return
        } else if (index !== -1) {
            setCartItems(cartItems.map(item => item.menuId === menu.menuId ? {...item, quantity} : item))
        } else {
            cartItems.push({menuId: menu.menuId, name: menu.name, price: menu.price, quantity})
        }
    }, [cartItems, removeItem, store])

    const updateItem = useCallback((itemIndex: number, quantity: number) => {
        if (quantity === 0) {
            removeItem(itemIndex)
            return
        }
        setCartItems(cartItems.map((item, index) => index === itemIndex ? {...item, quantity} : item))
    }, [cartItems, removeItem])

    const clear = useCallback(() => {
        setStore(undefined)
        setCartItems([])
    }, [])

    return (
        <cartContext.Provider value={{store, cartItems, addItem, updateItem, removeItem, clear}}>
            {children}
        </cartContext.Provider>
    )
}

export function useCart() {
    const context = useContext(cartContext)
    if (!context) throw new Error('useCart must be used in CartContext.')
    return context
}
