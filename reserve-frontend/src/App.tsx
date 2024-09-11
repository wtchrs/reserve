import React from 'react'
import {createBrowserRouter, RouterProvider, Link as RouterLink, LinkProps as RouterLinkProps} from 'react-router-dom'
import {ThemeProvider, createTheme} from '@mui/material'
import {LinkProps} from '@mui/material/Link'
import Layout from './components/Layout'
import Home from './components/Home'
import SignIn from './components/SignIn'
import SignUp from './components/SignUp'
import {AuthProvider} from './hooks/useAuth'
import UserDetailPage from './components/UserDetailPage'

const LinkBehavior = React.forwardRef<
    HTMLAnchorElement,
    Omit<RouterLinkProps, 'to'> & { href: RouterLinkProps['to'] }
>((props, ref) => {
    const {href, ...other} = props
    return <RouterLink data-testid="custom-link" ref={ref} to={href} {...other}/>
})

const theme = createTheme({
    components: {
        MuiLink: {
            defaultProps: {
                component: LinkBehavior,
            } as LinkProps,
        },
        MuiButtonBase: {
            defaultProps: {
                LinkComponent: LinkBehavior,
            },
        },
    },
})

const router = createBrowserRouter([
    {
        path: '/',
        element: <Layout/>,
        errorElement: <Layout showError/>,
        children: [
            {
                index: true,
                element: <Home/>,
            },
            {
                path: 'sign-in',
                element: <SignIn/>,
            },
            {
                path: 'sign-up',
                element: <SignUp/>,
            },
            {
                path: 'users/:username',
                element: <UserDetailPage/>,
            }
        ],
    },
])

function App() {
    return (
        <AuthProvider>
            <ThemeProvider theme={theme}>
                <RouterProvider router={router}/>
            </ThemeProvider>
        </AuthProvider>
    )
}

export default App
