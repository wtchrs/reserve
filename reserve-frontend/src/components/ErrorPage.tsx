import {Box, Button, Typography} from '@mui/material'
import {isRouteErrorResponse, useNavigate, useRouteError} from 'react-router-dom'
import {isAxiosError} from 'axios'

function ErrorPage() {
    const error = useRouteError();
    const navigate = useNavigate()

    let title = 'Error'
    let message = 'An unexpected error occurred. Please try again later.'
    let status = 0

    if (isRouteErrorResponse(error) || isAxiosError(error)) {
        switch (error.status) {
            case 400:
                title = 'Bad Request'
                message = 'The request could not be understood by the server due to malformed syntax.'
                status = 400
                break;
            case 401:
                navigate('/sign-in')
                break;
            case 403:
                title = 'Forbidden'
                message = 'You do not have permission to access this page.'
                status = 403
                break;
            case 404:
                title = 'Not Found'
                message = 'The requested page could not be found.'
                status = 404
                break;
            case 409:
                title = 'Conflict'
                message = 'A conflict occurred while processing the request.'
                status = 409
                break;
            case 500:
                title = 'Internal Server Error'
                message = 'An unexpected error occurred. Please try again later.'
                status = 500
                break;
        }
    }

    return (
        <Box textAlign="center" mt={5}>
            <Typography variant="h3" gutterBottom>
                {status ? `${status} - ${title}` : 'Error'}
            </Typography>
            <Typography variant="body1" paragraph>
                {message}
            </Typography>
            <Button variant="contained" onClick={() => navigate('/')}>
                Go to Home
            </Button>
        </Box>

    )
}

export default ErrorPage

