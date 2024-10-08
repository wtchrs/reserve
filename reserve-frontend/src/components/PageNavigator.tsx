import {Box, Button} from '@mui/material'

type Props = {
    hasPrevious: boolean
    hasNext: boolean
    onPageMove: (page: number) => void | Promise<void>
}

function PageNavigator({hasPrevious, hasNext, onPageMove}: Props) {
    return (
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
                variant="contained"
                disabled={!hasPrevious}
                onClick={async () => await onPageMove(-1)}
            >
                previous
            </Button>
            <Button
                variant="contained"
                disabled={!hasNext}
                onClick={async () => await onPageMove(1)}
            >
                next
            </Button>
        </Box>
    )
}

export default PageNavigator
