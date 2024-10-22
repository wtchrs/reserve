import {createContext, ReactNode, useCallback, useContext, useState} from 'react'
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

export function CartProvider({children}: { children: ReactNode }) {
    const [cart, setCart] = useState<Cart>({items: []})

    const removeItem = useCallback((itemIndex: number) => {
        const newCartItems = cart.items.slice()
        newCartItems.splice(itemIndex, 1)
        setCart({...cart, items: newCartItems})
        if (newCartItems.length === 0) setCart({items: []})
    }, [cart])

    const addItem = useCallback((newStore: Store, menu: Menu, quantity: number) => {
        if (cart.store && cart.store.storeId !== newStore.storeId) {
            const newCartItems: CartItem[] = [{menuId: menu.menuId, name: menu.name, price: menu.price, quantity}]
            setCart({store: newStore, items: newCartItems})
            return
        }

        const index = cart.items.findIndex(item => item.menuId === menu.menuId)

        if (index !== -1 && quantity === 0) {
            removeItem(index)
            return
        } else if (index !== -1) {
            setCart({
                store: newStore,
                items: cart.items.map(item => item.menuId === menu.menuId ? {...item, quantity} : item),
            })
        } else {
            setCart({
                store: newStore,
                items: [...cart.items, {menuId: menu.menuId, name: menu.name, price: menu.price, quantity}],
            })
        }
    }, [cart, removeItem])

    const updateItem = useCallback((itemIndex: number, quantity: number) => {
        if (quantity === 0) {
            removeItem(itemIndex)
            return
        }
        setCart({
            store: cart.store,
            items: cart.items.map((item, index) => index === itemIndex ? {...item, quantity} : item),
        })
    }, [cart, removeItem])

    const clear = useCallback(() => {
        setCart({items: []})
    }, [])

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
