import {useCart} from '@hooks/useCart.tsx'
import {useCallback, useState, MouseEvent} from 'react'
import {Badge, Box, IconButton} from '@mui/material'
import {ShoppingCart} from '@mui/icons-material'
import CartPopup from './CartPopup.tsx'

function CartPopupButton() {
    const [anchorEl, setAnchorEl] = useState<Element>()
    const {cart: {store}} = useCart()

    const handleClick = useCallback((event: MouseEvent) => {
        setAnchorEl(event.currentTarget)
    }, [])

    const handleClose = useCallback(() => {
        setAnchorEl(undefined)
    }, [])

    const open = Boolean(anchorEl)
    const id = open ? 'cart-popup' : undefined

    return (
        <Box>
            <IconButton aria-describedby={id} color="primary" sx={{mr: 2}} onClick={handleClick}>
                <Badge color="info" variant="dot" invisible={!store}>
                    <ShoppingCart/>
                </Badge>
            </IconButton>
            <CartPopup anchorEl={anchorEl} id={id} handleClose={handleClose}/>
        </Box>
    )
}

export default CartPopupButton
