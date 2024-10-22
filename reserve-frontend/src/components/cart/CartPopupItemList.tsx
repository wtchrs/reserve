import {Divider, Stack, Typography} from '@mui/material'
import type {CartItem} from '../../../types/domain.d.ts'
import CartPopupItem from './CartPopupItem.tsx'

type Props = {
    items: CartItem[]
}

function CartPopupItemList({items}: Props) {
    return (
        <Stack spacing={2} divider={<Divider orientation="horizontal" flexItem/>}>
            {items.length === 0 && <Typography>No items in cart.</Typography>}
            {items.length > 0 && items.map((item, index) => <CartPopupItem index={index} item={item}/>)}
        </Stack>
    )
}

export default CartPopupItemList
