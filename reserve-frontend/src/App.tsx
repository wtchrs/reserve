import React from 'react'
import {createBrowserRouter, RouterProvider, Link as RouterLink, LinkProps as RouterLinkProps} from 'react-router-dom'
import {ThemeProvider, createTheme} from '@mui/material'
import {LinkProps} from '@mui/material/Link'
import Layout from './components/Layout'
import Home from './components/Home'
import SignInPage from './components/auth/SignInPage'
import SignUpPage from './components/auth/SignUpPage'
import StoreCreatePage from './components/store/StoreCreatePage'
import StoreDetailPage from './components/store/StoreDetailPage'
import {AuthProvider} from './hooks/useAuth'
import UserDetailPage from './components/user/UserDetailPage'
import UserUpdatePage from './components/user/UserUpdatePage'
import MyPage from './components/user/MyPage'
import PasswordUpdatePage from './components/user/PasswordUpdatePage'
import UserSearchPage from './components/user/UserSearchPage'
import StoreSearchPage from './components/store/StoreSearchPage'

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
                element: <SignInPage/>,
            },
            {
                path: 'sign-up',
                element: <SignUpPage/>,
            },
            {
                path: 'mypage',
                element: <MyPage/>,
            },
            {
                path: 'users',
                element: <UserSearchPage/>,
            },
            {
                path: 'users/:username',
                element: <UserDetailPage/>,
            },
            {
                path: 'users/edit',
                element: <UserUpdatePage/>,
            },
            {
                path: 'users/password',
                element: <PasswordUpdatePage/>,
            },
            {
                path: 'stores',
                element: <StoreSearchPage/>,
            },
            {
                path: 'stores/register',
                element: <StoreCreatePage/>,
            },
            {
                path: 'stores/:storeId',
                element: <StoreDetailPage/>,
            },
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
