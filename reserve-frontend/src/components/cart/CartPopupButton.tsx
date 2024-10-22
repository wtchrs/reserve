import {useCallback, useState, MouseEvent} from 'react'
import {Box, IconButton} from '@mui/material'
import {ShoppingCart} from '@mui/icons-material'
import CartPopup from './CartPopup.tsx'

function CartPopupButton() {
    const [anchorEl, setAnchorEl] = useState<Element>()

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
                <ShoppingCart/>
            </IconButton>
            <CartPopup anchorEl={anchorEl} id={id} handleClose={handleClose}/>
        </Box>
    )
}

export default CartPopupButton
