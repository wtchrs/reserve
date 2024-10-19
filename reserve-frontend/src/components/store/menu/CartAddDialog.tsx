import {zodResolver} from '@hookform/resolvers/zod'
import {
    Box,
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    Grid,
    TextField,
    Typography,
} from '@mui/material'
import {useCallback, useEffect, useState} from 'react'
import {useForm} from 'react-hook-form'
import {z} from 'zod'
import {useCart} from '../../../hooks/useCart.tsx'
import {Menu, Store} from '../../../type.ts'

const schema = z.object({
    quantity: z.number().min(1, 'Quantity must be at least 1').max(100, 'Quantity must be at most 100'),
})

type SchemaType = z.infer<typeof schema>

type Props = {
    store: Store
    menu: Menu
    onClose: () => void
}

function CartAddDialog({store, menu, onClose}: Props) {
    const {cartItems, addItem} = useCart()
    const [quantity, setQuantity] = useState(1)
    const {
        register,
        handleSubmit,
        getValues,
        reset,
        formState: {errors, isValid},
    } = useForm<SchemaType>({resolver: zodResolver(schema), mode: 'onChange'})

    const hasFieldError = (field: keyof SchemaType) => field in errors

    useEffect(() => {
        reset({quantity: 1})
    }, [])

    const onSubmit = useCallback((input: SchemaType) => {
        const item = cartItems.find(item => item.menuId === menu.menuId)
        if (item) {
            addItem(store, menu, item.quantity + input.quantity)
        } else {
            addItem(store, menu, input.quantity)
        }
        onClose()
    }, [cartItems, menu, store])

    return (
        <Dialog open={true} onClose={onClose}>
            <Box sx={{width: '25rem'}}>
                <DialogTitle>Add to Cart</DialogTitle>
                <DialogContent>
                    <Typography variant="h6">{menu.name}</Typography>
                    <Typography variant="body2">Price: {menu.price}</Typography>
                </DialogContent>
                <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{mx: 2, mb: 2}}>
                    <Grid container spacing={2} width="auto">
                        <Grid item xs={12}>
                            <TextField
                                required fullWidth id="quantity" label="Quantity" type="number"
                                inputProps={{min: 1, max: 100}}
                                error={hasFieldError('quantity')}
                                {...register('quantity', {
                                    valueAsNumber: true,
                                    onChange: () => setQuantity(getValues('quantity')),
                                })}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Typography variant="body2">
                                Total: {menu.price * quantity}
                            </Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Button fullWidth variant="outlined" onClick={onClose}>Cancel</Button>
                        </Grid>
                        <Grid item xs={6}>
                            <Button fullWidth variant="contained" color="primary" type="submit" disabled={!isValid}>
                                Add to Cart
                            </Button>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Dialog>
    )
}

export default CartAddDialog
