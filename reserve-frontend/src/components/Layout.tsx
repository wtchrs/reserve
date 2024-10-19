import {Button, Container, CssBaseline, Tabs, Tab, Toolbar, Typography, Box, SxProps} from '@mui/material'
import {Link as RouterLink, Outlet} from 'react-router-dom'
import Link from '@mui/material/Link'
import useRouteMatch from '../hooks/useRouteMatch'
import AuthInfo from './auth/AuthInfo.tsx'
import CartPopupButton from './cart/CartPopupButton.tsx'
import ErrorPage from './ErrorPage'

function Copyright(props: { sx: SxProps }) {
    return (
        <Typography variant="body2" color="text.secondary" align="center" {...props}>
            {'Copyright © '}
            <Link color="inherit" href="/">
                Reserve
            </Link>
            {' 2024.'}
        </Typography>
    )
}

type Props = {
    showError?: boolean
}

function Layout({showError}: Props) {
    const routeMatch = useRouteMatch(['/', '/users', '/users/:username', '/stores', '/stores/:storeId', '/stores/register'])
    let currentTab = routeMatch?.pattern?.path || false
    currentTab = currentTab && /^\/users/.test(currentTab) ? '/users' : currentTab
    currentTab = currentTab && /^\/stores/.test(currentTab) ? '/stores' : currentTab

    return (
        <>
            <CssBaseline/>
            <Container sx={{
                display: 'flex',
                flexDirection: 'column',
                minHeight: '100vh',
            }}>
                <Box pb="1rem">
                    <Toolbar>
                        <Button href="/">
                            <Typography variant="h2" color="black">Reserve</Typography>
                        </Button>
                        <Box px="1rem"></Box>
                        <Tabs value={currentTab} sx={{flexGrow: 1}}>
                            <Tab label="Home" value="/" to="/" component={RouterLink}/>
                            <Tab label="Users" value="/users" to="/users" component={RouterLink}/>
                            <Tab label="Stores" value="/stores" to="/stores" component={RouterLink}/>
                        </Tabs>
                        <CartPopupButton/>
                        <AuthInfo/>
                    </Toolbar>
                </Box>
                <Container component="main" sx={{flexGrow: 1}}>
                    {showError ? <ErrorPage/> : <Outlet/>}
                </Container>
                <Copyright sx={{mt: 8, mb: 4}}/>
            </Container>
        </>
    )
}

export default Layout
