import {MouseEvent, useCallback, useEffect, useState} from 'react'
import {Box, Button, CircularProgress, Typography} from '@mui/material'
import menuService from '../../../services/menuService.ts'
import {Menu} from '../../../type.ts'
import ErrorMessages from '../../ErrorMessages.tsx'
import MenuCreateDialog from './MenuCreateDialog.tsx'
import MenuDeleteDialog from './MenuDeleteDialog.tsx'
import MenuUpdateDialog from './MenuUpdateDialog.tsx'

type Props = {
    storeId: string
}

function MenuListUpdate({storeId}: Props) {
    const [menuCreateDialog, setMenuCreateDialog] = useState(false)
    const [menuUpdate, setMenuUpdate] = useState<Menu | null>()
    const [menuDelete, setMenuDelete] = useState<Menu | null>()
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
        if (!storeId) return
        loadMenus(storeId).catch(_err => setError('Something went wrong. Please try again later.'))
    }, [storeId, loadMenus])

    const handleDelete = (menu: Menu) => {
        return (event: MouseEvent) => {
            event.stopPropagation()
            setMenuDelete(menu)
        }
    }

    return (
        <Box sx={{mt: 5, width: 'auto', textAlign: 'left'}}>
            <Box sx={{mb: 3, ml: 2, display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                <Typography variant="h5">Menu List</Typography>
                <Button variant="contained" onClick={() => setMenuCreateDialog(true)}>Create Menu</Button>
            </Box>

            <Box>
                {loading && <CircularProgress sx={{display: 'block', margin: '0 auto', mt: 4}}/>}
                {!loading && menus.length === 0 && <Typography>No menus found.</Typography>}
                {error && <ErrorMessages errors={error}/>}
                {!loading && menus.length > 0 && (
                    menus.map(menu => {
                        return (
                            <Box
                                key={menu.menuId}
                                onClick={() => setMenuUpdate(menu)}
                                sx={{
                                    border: '1px solid gray',
                                    borderRadius: 1,
                                    padding: 2,
                                    marginTop: 2,
                                    width: 'auto',
                                    cursor: 'pointer',
                                    ':hover': {
                                        boxShadow: 2,
                                        '.delete-button': {
                                            display: 'inline-flex',
                                        },
                                    },
                                }}
                            >
                                <Box sx={{display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                                    <Box>
                                        <Typography variant="h6">{menu.name}</Typography>
                                        <Typography variant="body2">{menu.price}</Typography>
                                        <Typography variant="body1">{menu.description}</Typography>
                                    </Box>
                                    <Box sx={{display: 'flex', flexDirection: 'column', justifyContent: 'center'}}>
                                        <Button
                                            variant="outlined"
                                            color="error"
                                            onClick={handleDelete(menu)}
                                            className="delete-button"
                                            sx={{display: 'none'}}
                                        >
                                            Delete
                                        </Button>
                                    </Box>
                                </Box>
                            </Box>
                        )
                    })
                )}
            </Box>

            {menuCreateDialog && (
                <MenuCreateDialog
                    storeId={storeId}
                    onClose={() => setMenuCreateDialog(false)}
                    onCreated={async () => await loadMenus(storeId)}
                />
            )}

            {menuUpdate && (
                <MenuUpdateDialog
                    menu={menuUpdate}
                    onClose={() => setMenuUpdate(null)}
                    onUpdated={async () => await loadMenus(storeId)}
                />
            )}

            {menuDelete && (
                <MenuDeleteDialog
                    menu={menuDelete}
                    onClose={() => setMenuDelete(null)}
                    onDeleted={async () => await loadMenus(storeId)}
                />
            )}
        </Box>
    )
}

export default MenuListUpdate
