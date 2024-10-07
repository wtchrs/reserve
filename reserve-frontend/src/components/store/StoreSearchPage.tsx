import {useState} from 'react'
import {Box, Button, CircularProgress, Grid, TextField, Typography} from '@mui/material'
import {zodResolver} from '@hookform/resolvers/zod'
import {SubmitHandler, useForm} from 'react-hook-form'
import {useNavigate} from 'react-router-dom'
import {SearchStoreParams, searchStoreSchema} from '../../schema'
import ErrorMessages from '../ErrorMessages'
import storeService from '../../services/storeService'
import {PageParams, Store} from '../../type'

function StoreSearchPage() {
    const navigate = useNavigate()

    const {
        handleSubmit,
        register,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<SearchStoreParams>({resolver: zodResolver(searchStoreSchema), mode: 'onChange'})

    // TODO: Replace the page and searchParams state with query params

    const [page, setPage] = useState<PageParams<Store>>({
        page: 0,
        size: import.meta.env.VITE_DEFAULT_PAGE_SIZE,
        sort: [],
    })

    const [searchParams, setSearchParams] = useState<SearchStoreParams>({})

    const [loading, setLoading] = useState(false)
    const [hasNext, setHasNext] = useState(false)
    const [stores, setStores] = useState<Store[]>([])

    const hasFieldError = (field: string) => field in fieldErrors

    const fetchStores = async (params: SearchStoreParams, page: PageParams<Store>) => {
        setLoading(true)
        try {
            const res = await storeService.search(params, page)
            setStores(res.results)
            setHasNext(res.hasNext)
        } catch (_err) {
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
        setLoading(false)
    }

    const onSubmit: SubmitHandler<SearchStoreParams> = async params => {
        setSearchParams(params)
        setPage(prev => ({...prev, page: 0}))
        await fetchStores(params, page)
    }

    const onPageMove = async (move: number) => {
        const newPage = {
            ...page,
            page: Math.max(0, page.page + move),
        }
        await fetchStores(searchParams, newPage)
        setPage(newPage)
    }

    const handleRouteStore = (storeId: bigint) => {
        navigate(`/stores/${storeId}`)
    }

    return (
        <Box sx={{padding: 4}}>
            <Typography variant="h4" align="center" gutterBottom>
                Store Search
            </Typography>

            <Box
                component="form"
                onSubmit={handleSubmit(onSubmit)}
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 2,
                    maxWidth: 400,
                    margin: '0 auto',
                }}
            >
                <Grid container spacing={2}>
                    <Grid container item spacing={2} xs={9}>
                        <Grid item xs={12}>
                            <TextField fullWidth id="registrant" label="Registrant Username" {...register('registrant')}
                                       error={hasFieldError('registrant')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth id="query" label="Search Query" {...register('query')}
                                       error={hasFieldError('query')}/>
                        </Grid>
                    </Grid>
                    <Grid item xs={3}>
                        <Button type="submit" variant="contained" fullWidth sx={{height: '100%'}}
                                disabled={loading || !isValid}>
                            {loading ? <CircularProgress size={24}/> : 'Search'}
                        </Button>
                    </Grid>
                </Grid>

                <ErrorMessages errors={fieldErrors}/>
            </Box>


            {stores.length > 0 && (
                <Box sx={{mt: 4}}>
                    <Typography variant="h6" gutterBottom>
                        Search Results
                    </Typography>
                    <Grid container spacing={2} columns={{xs: 4, sm: 8, md: 12}}>
                        {stores.map((store, index) => (
                            <Grid item key={index} xs={4}>
                                <Box
                                    onClick={() => handleRouteStore(store.storeId)}
                                    sx={{
                                        p: 2,
                                        height: '100%',
                                        border: '1px solid gray',
                                        borderRadius: '8px',
                                        cursor: 'pointer',
                                        ':hover': {
                                            boxShadow: 2,
                                        },
                                    }}
                                >
                                    <Typography variant="h6">{store.name}</Typography>
                                    <Typography variant="caption">{store.registrant}</Typography>
                                    <Typography>{store.description}</Typography>
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            )}

            {!loading && stores.length === 0 && (
                <Typography align="center" sx={{mt: 2}}>
                    No results found
                </Typography>
            )}

            <Box sx={{
                marginTop: 3,
                display: 'flex',
                gap: 1,
                alignItems: 'center',
                justifyContent: 'center',
                flexDirection: 'row',
                width: 'auto',
            }}>
                <Button
                    variant="contained" disabled={page.page === 0}
                    onClick={async () => await onPageMove(-1)}
                >
                    previous
                </Button>
                <Button
                    variant="contained"
                    disabled={!hasNext} onClick={async () => await onPageMove(1)}
                >
                    next
                </Button>
            </Box>
        </Box>
    )
}

export default StoreSearchPage
