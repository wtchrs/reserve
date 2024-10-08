import {useState} from 'react'
import {Box, Button, CircularProgress, Grid, TextField, Typography} from '@mui/material'
import {zodResolver} from '@hookform/resolvers/zod'
import {SubmitHandler, useForm} from 'react-hook-form'
import {useNavigate} from 'react-router-dom'
import {SearchStoreParams, searchStoreSchema} from '../../schema'
import ErrorMessages from '../ErrorMessages'
import storeService from '../../services/storeService'
import {PageParams, Store} from '../../type'
import PageNavigator from '../PageNavigator.tsx'
import StoreList from './StoreList.tsx'

function StoreSearchPage() {
    const navigate = useNavigate()

    const {
        handleSubmit,
        register,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<SearchStoreParams>({resolver: zodResolver(searchStoreSchema), mode: 'onChange'})

    // TODO: Replace the pageParam and searchParams state with query params

    const [pageParam, setPageParam] = useState<PageParams<Store>>({
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
        setPageParam(prev => ({...prev, page: 0}))
        await fetchStores(params, pageParam)
    }

    const onPageMove = async (move: number) => {
        const newPage = {
            ...pageParam,
            page: Math.max(0, pageParam.page + move),
        }
        await fetchStores(searchParams, newPage)
        setPageParam(newPage)
    }

    return (
        <Box sx={{mb: 4}}>
            <Box sx={{display: 'flex', flexDirection: 'row'}}>
                <Button variant="text" onClick={() => navigate(-1)} sx={{mb: 3, textTransform: 'none'}}>
                    {'< Go Back'}
                </Button>
                <Button variant="contained" onClick={() => navigate('/stores/register')}
                        sx={{marginBottom: 3, marginLeft: 'auto'}}>
                    Register Store
                </Button>
            </Box>

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
                    <StoreList stores={stores}/>
                </Box>
            )}

            {!loading && stores.length === 0 && (
                <Typography align="center" sx={{mt: 2}}>
                    No results found
                </Typography>
            )}

            {loading && (
                <CircularProgress sx={{display: 'block', margin: '0 auto', mt: 4}}/>
            )}

            <PageNavigator hasPrevious={pageParam.page > 0} hasNext={hasNext} onPageMove={onPageMove}/>
        </Box>
    )
}

export default StoreSearchPage
