import {Clear} from '@mui/icons-material'
import {Box, IconButton, MenuItem, Select, Typography} from '@mui/material'
import {useCart} from '../../hooks/useCart.tsx'
import {CartItem} from '../../type.ts'

type Props = {
    item: CartItem
    index: number
}

function CartPopupItem({item, index}: Props) {
    const {updateItem, removeItem} = useCart()

    return (
        <Box
            key={item.menuId}
            sx={{display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between'}}
        >
            <Box>
                <Typography>{item.name}</Typography>
                <Typography variant="body2">price: {item.price * item.quantity}</Typography>
            </Box>
            <Box sx={{
                height: '100%',
                width: '3rem',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'end',
            }}>
                <Box sx={{
                    display: 'flex',
                    flexDirection: 'row',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                }}>
                    <Select
                        variant="outlined" value={item.quantity}
                        onChange={e => updateItem(index, Number(e.target.value))}
                    >
                        {[...Array(100).keys()].map(i => (
                            <MenuItem key={i} value={i}>{i}</MenuItem>
                        ))}
                    </Select>
                    <IconButton onClick={() => removeItem(index)}><Clear/></IconButton>
                </Box>
            </Box>
        </Box>
    )
}

export default CartPopupItem
