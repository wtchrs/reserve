import {Button, Container, CssBaseline, Tabs, Tab, Toolbar, Typography, Box, SxProps} from '@mui/material'
import {Link as RouterLink, Outlet} from 'react-router-dom'
import Link from '@mui/material/Link'
import useRouteMatch from '../hooks/useRouteMatch'

function Copyright(props: { sx: SxProps }) {
    return (
        <Typography variant="body2" color="text.secondary" align="center" {...props}>
            {'Copyright © '}
            <Link color="inherit" href="/">
                Reserve
            </Link>
            {' '}
            {new Date().getFullYear()}
            {'.'}
        </Typography>
    )
}


function Layout() {
    const routeMatch = useRouteMatch(['/'])
    const currentTab = routeMatch?.pattern?.path || false

    return (
        <>
            <CssBaseline/>
            <Container>
                <Box pb="1rem">
                    <Toolbar>
                        <Button href="/">
                            <Typography variant="h2" color="black">Reserve</Typography>
                        </Button>
                        <Box px="1rem"></Box>
                        <Tabs value={currentTab} sx={{flexGrow: 1}}>
                            <Tab label="Home" value="/" to="/" component={RouterLink}/>
                        </Tabs>
                        <Box sx={{display: 'flex', gap: 2}}>
                            <Button href="/sign-up" variant="outlined">Sign up</Button>
                            <Button href="/sign-in" variant="contained">Sign in</Button>
                        </Box>
                    </Toolbar>
                </Box>
                <Container component="main">
                    <Outlet/>
                </Container>
                <Copyright sx={{mt: 8, mb: 4}}/>
            </Container>
        </>
    )
}

export default Layout
