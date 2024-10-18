import {createContext, ReactNode, useCallback, useContext, useState} from 'react'
import {CartItem, Menu, Store} from '../type.ts'

type CartContext = {
    store?: Store
    items: CartItem[]
    setItem: (store: Store, item: Menu, quantity: number) => void
    remove: (menuId: bigint) => void
    clear: () => void
}

const cartContext = createContext<CartContext>({} as CartContext)

export function CartProvider({children}: { children: ReactNode }) {
    const [store, setStore] = useState<Store>()
    const [cartItem, setCartItem] = useState<CartItem[]>([])

    const remove = useCallback((menuId: bigint) => {
        const index = cartItem.findIndex(item => item.menuId === menuId)
        if (index !== -1) {
            cartItem.splice(index, 1)
            if (cartItem.length === 0) setStore(undefined)
        }
    }, [cartItem])

    const setItem = useCallback((newStore: Store, menu: Menu, quantity: number) => {
        if (store && store.storeId !== newStore.storeId) {
            setStore(newStore)
            setCartItem([{menuId: menu.menuId, name: menu.name, price: menu.price, quantity}])
            return
        }

        const index = cartItem.findIndex(item => item.menuId === menu.menuId)

        if (index !== -1 && quantity === 0) {
            remove(menu.menuId)
            return
        } else if (index !== -1) {
            setCartItem(cartItem.map(item => item.menuId === menu.menuId ? {...item, quantity} : item))
        } else {
            cartItem.push({menuId: menu.menuId, name: menu.name, price: menu.price, quantity})
        }
    }, [cartItem, remove, store])

    const clear = useCallback(() => {
        cartItem.splice(0, cartItem.length)
    }, [cartItem])

    return (
        <cartContext.Provider value={{items: cartItem, setItem: setItem, remove, clear}}>
            {children}
        </cartContext.Provider>
    )
}

export function useCart() {
    const context = useContext(cartContext)
    if (!context) throw new Error('useCart must be used in CartContext.')
    return context
}
