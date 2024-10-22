import {ShoppingCart, Store} from '@mui/icons-material'
import {Box, Button, Divider, Popover, Typography} from '@mui/material'
import {useCart} from '@hooks/useCart.tsx'
import CartPopupItemList from './CartPopupItemList.tsx'

type Props = {
    anchorEl?: Element
    id?: string
    handleClose: () => void
}

function CartPopup({anchorEl, id, handleClose}: Props) {
    const {cart, clear} = useCart()

    const open = Boolean(anchorEl)

    return (
        <Popover
            id={id}
            open={open}
            anchorEl={anchorEl}
            onClose={handleClose}
            anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'center',
            }}
            transformOrigin={{
                vertical: 'top',
                horizontal: 'center',
            }}
        >
            <Box sx={{width: '25rem'}}>
                <Box sx={{p: 2, display: 'flex', flexDirection: 'row', alignItems: 'center'}}>
                    <ShoppingCart sx={{mr: 1}}/>
                    <Typography variant="h5">Cart</Typography>
                </Box>
                <Divider sx={{borderColor: '#bbb'}}/>
                {cart.store && (
                    <>
                        <Button
                            fullWidth color="inherit"
                            href={`/stores/${cart.store.storeId}`}
                            sx={{
                                p: 2,
                                display: 'flex',
                                flexDirection: 'row',
                                alignItems: 'center',
                                justifyContent: 'flex-start',
                                textTransform: 'none',
                            }}
                        >
                            <Store sx={{mr: 1}}/>
                            <Typography variant="h6">{cart.store.name}</Typography>
                        </Button>
                        <Divider sx={{borderColor: '#ccc'}}/>
                    </>
                )}
                <Box sx={{p: 2, backgroundColor: 'white'}}>
                    <CartPopupItemList items={cart.items}/>
                </Box>
                <Divider sx={{borderColor: '#bbb'}}/>
                <Box sx={{p: 2}}>
                    <Typography variant="h6" sx={{color: '#111'}}>
                        Total: {cart.items.reduce((acc, item) => acc + item.price * item.quantity, 0)}
                    </Typography>
                </Box>
                <Divider sx={{borderColor: '#bbb'}}/>
                <Box sx={{p: 2, display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                    <Button variant="text" color="error" onClick={clear}>Clear</Button>
                    <Button variant="contained" color="primary">Reserve</Button>
                </Box>
            </Box>
        </Popover>
    )
}

export default CartPopup
