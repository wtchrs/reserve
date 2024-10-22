import {Box, CircularProgress, Grid, Typography} from '@mui/material'
import {useCallback, useEffect, useState} from 'react'
import type {Menu, Store} from '../../../../types/domain.d.ts'
import ErrorMessages from '../../ErrorMessages.tsx'
import menuService from '../../../services/menuService.ts'
import CartAddDialog from './CartAddDialog.tsx'

type Props = { store: Store }

function MenuList({store}: Props) {
    const [menus, setMenus] = useState<Menu[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [cartAddItem, setCartAddItem] = useState<Menu>()

    const loadMenus = useCallback(async (storeId: string) => {
        setLoading(true)
        const res = await menuService.getMenuListForStore(storeId)
        setMenus(res.results)
        setLoading(false)
    }, [])

    useEffect(() => {
        loadMenus(store.storeId.toString()).catch(_err => setError('Something went wrong. Please try again later.'))
    }, [loadMenus, store])

    const handleCloseDialog = useCallback(() => {
        setCartAddItem(undefined)
    }, [])

    return (
        <Box sx={{mt: 5, width: 'auto', textAlign: 'left'}}>
            <Box sx={{mb: 3, ml: 2, display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                <Typography variant="h5" sx={{}}>Menu List</Typography>
            </Box>

            <Box>
                {loading && <CircularProgress sx={{display: 'block', margin: '0 auto', mt: 4}}/>}
                {!loading && menus.length === 0 && <Typography>No menus found.</Typography>}
                {error && <ErrorMessages errors={error}/>}
                {!loading && menus.length > 0 && (
                    <Grid container spacing={2} columns={{sm: 6, md: 12}}>
                        {menus.map(menu => (
                            <Grid item xs={6} key={menu.menuId}>
                                <Box
                                    onClick={() => setCartAddItem(menu)}
                                    sx={{
                                        border: '1px solid gray',
                                        borderRadius: 1,
                                        padding: 2,
                                        cursor: 'pointer',
                                        ':hover': {
                                            boxShadow: 2,
                                        },
                                    }}
                                >
                                    <Typography variant="h6">{menu.name}</Typography>
                                    <Typography variant="body2">{menu.price}</Typography>
                                    <Typography variant="body1">{menu.description}</Typography>
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                )}
            </Box>

            {cartAddItem && <CartAddDialog store={store} menu={cartAddItem} onClose={handleCloseDialog}/>}
        </Box>
    )
}

export default MenuList
