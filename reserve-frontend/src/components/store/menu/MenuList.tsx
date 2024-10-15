import {Box, CircularProgress, Typography} from '@mui/material'
import {useCallback, useEffect, useState} from 'react'
import {Menu} from '../../../type.ts'
import ErrorMessages from '../../ErrorMessages.tsx'
import menuService from '../../../services/menuService.ts'

type Props = { storeId: string }

function MenuList({storeId}: Props) {
    const [menus, setMenus] = useState<Menu[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const loadMenus = useCallback(async (storeId: string) => {
        setLoading(true)
        const res = await menuService.getMenuListForStore(storeId)
        setMenus(res.results)
        setLoading(false)
    }, [])

    useEffect(() => {
        loadMenus(storeId).catch(_err => setError('Something went wrong. Please try again later.'))
    }, [loadMenus, storeId])

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
                    menus.map(menu => (
                        <Box key={menu.menuId} sx={{
                            border: '1px solid gray',
                            borderRadius: 1,
                            padding: 2,
                            marginTop: 2,
                            width: 'auto',
                            cursor: 'pointer',
                            ':hover': {
                                boxShadow: 2,
                            },
                        }}>
                            <Typography variant="h6">{menu.name}</Typography>
                            <Typography variant="body2">{menu.price}</Typography>
                            <Typography variant="body1">{menu.description}</Typography>
                        </Box>
                    ))
                )}
            </Box>
        </Box>
    )
}

export default MenuList
