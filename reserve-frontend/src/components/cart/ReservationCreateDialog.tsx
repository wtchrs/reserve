import ErrorMessages from '@components/ErrorMessages.tsx'
import FlashMessageDialog from '@components/FlashMessageDialog.tsx'
import LoadingDialog from '@components/LoadingDialog.tsx'
import {zodResolver} from '@hookform/resolvers/zod'
import {useCart} from '@hooks/useCart.tsx'
import {
    Box,
    Button,
    Dialog,
    DialogTitle,
    Divider,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Stack, Typography,
} from '@mui/material'
import {DatePicker} from '@mui/x-date-pickers/DatePicker'
import reservationService from '@services/reservationService.ts'
import {logOnDev} from '@utils/log.ts'
import dayjs from 'dayjs'
import {CreateReservationRequest, createReservationSchema} from '@/schema.ts'
import {Cart} from '@customTypes/domain'
import {useCallback, useState} from 'react'
import {Controller, useForm} from 'react-hook-form'

type Props = {
    cart: Cart
    onClose: () => void
}

function ReservationCreateDialog({cart, onClose}: Props) {
    const {clear} = useCart()

    const {
        handleSubmit,
        control,
        setError,
        formState: {errors, isValid},
    } = useForm<CreateReservationRequest>({
        resolver: zodResolver(createReservationSchema),
        mode: 'onChange',
        defaultValues: {
            storeId: cart.store?.storeId,
            date: dayjs().add(1, 'hour').toDate(),
            hour: dayjs().add(1, 'hour').hour(),
            menus: cart.items.map(item => ({menuId: item.menuId, quantity: item.quantity})),
        },
    })

    const [loading, setLoading] = useState(false)
    const [finished, setFinished] = useState(false)

    const onSubmit = useCallback(async (request: CreateReservationRequest) => {
        logOnDev('request', request)
        setLoading(true)
        try {
            await reservationService.create(request)
            clear()
            setFinished(true)
        } catch (err) {
            logOnDev('err', err)
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
    }, [setError])

    const onCloseFlash = useCallback(() => {
        setFinished(false)
        onClose()
    }, [onClose])

    return (
        <Dialog open={true} onClose={onClose}>
            <DialogTitle>Create Reservation</DialogTitle>

            <Divider/>

            <Box sx={{maxHeight: '25rem', overflow: 'hidden', overflowY: 'auto'}}>
                <Stack spacing={2} divider={<Divider/>} sx={{mx: 3, my: 2}}>
                    {cart.items.map(item => (
                        <Box key={item.menuId}>
                            <Box display="flex" justifyContent="space-between">
                                <Typography variant="body1">{item.name}</Typography>
                                <Typography variant="body1">{item.price}</Typography>
                            </Box>
                            <Box display="flex" justifyContent="space-between">
                                <Typography variant="body2">Quantity: {item.quantity}</Typography>
                                <Typography variant="body2">Subtotal: {item.price * item.quantity}</Typography>
                            </Box>
                        </Box>
                    ))}
                </Stack>
            </Box>

            <Divider/>

            <Box component="form" sx={{width: '25rem', m: 3}} onSubmit={handleSubmit(onSubmit)}>
                <Grid container spacing={2}>
                    {errors && (
                        <Grid item xs={12}>
                            <ErrorMessages errors={errors}/>
                        </Grid>
                    )}
                    <Grid item xs={8}>
                        <FormControl fullWidth>
                            <Controller
                                name="date"
                                control={control}
                                render={({field}) => (
                                    <DatePicker
                                        label="Reservation Date"
                                        {...field}
                                        value={dayjs(field.value)}
                                        onChange={(date) => field.onChange(date?.toDate() || new Date())}
                                    />
                                )}
                            />
                        </FormControl>
                    </Grid>
                    <Grid item xs={4}>
                        <FormControl fullWidth>
                            <InputLabel id="hour-select">Hour</InputLabel>
                            <Controller
                                name="hour"
                                control={control}
                                render={({field}) => (
                                    <Select
                                        labelId="hour-select"
                                        label="Hour"
                                        {...field}
                                    >
                                        {[...Array(24)].map((_, i) => (
                                            <MenuItem key={i} value={i}>{String(i).padStart(2, '0') + ':00'}</MenuItem>
                                        ))}
                                    </Select>
                                )}
                            />
                        </FormControl>
                    </Grid>
                    <Grid item xs={6}>
                        <Button fullWidth variant="outlined" color="primary" onClick={onClose}>
                            Cancel
                        </Button>
                    </Grid>
                    <Grid item xs={6}>
                        <Button type="submit" fullWidth variant="contained" color="primary" disabled={!isValid}>
                            Reserve
                        </Button>
                    </Grid>
                </Grid>
            </Box>

            {loading && <LoadingDialog/>}
            {finished && (
                <FlashMessageDialog open={true} message="Reservation created successfully." onClose={onCloseFlash}/>
            )}
        </Dialog>
    )
}

export default ReservationCreateDialog
