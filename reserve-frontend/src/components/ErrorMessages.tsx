import {Box, Typography} from '@mui/material'
import {ErrorOutlined} from '@mui/icons-material'
import {FieldErrors} from 'react-hook-form'

type Error = { key: string, message: string }
type Props = { errors: string | Error[] | FieldErrors }

function isFieldErrors(errors: string | Error[] | FieldErrors): errors is FieldErrors {
    return typeof errors === 'object' && !Array.isArray(errors)
}

function ErrorMessages({errors}: Props) {
    if (typeof errors === 'string') {
        errors = [{key: 'error', message: errors}]
    }

    if (isFieldErrors(errors)) {
        errors = Object.entries(errors)
            .filter(([_, value]) => !!value?.message)
            .map(([field, value]) => ({key: field, message: value?.message as string}))
    }

    return (
        <Box sx={{
            mb: 2,
            p: 2,
            backgroundColor: 'rgba(255, 0, 0, 0.1)', // Light red background
            border: '1px solid red',
            borderRadius: '8px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'start',
        }}>
            {errors.map(({key, message}) => (
                <Box key={key} sx={{display: 'flex', alignItems: 'center', mb: 0.5}}>
                    <ErrorOutlined sx={{color: 'red', mr: 1}}/>
                    <Typography color="error">
                        {message}
                    </Typography>
                </Box>
            ))}
        </Box>
    )
}

export default ErrorMessages
