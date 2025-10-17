import axios from 'axios'
import type { EndpointVM } from './types'


export async function fetchEndpoints(): Promise<EndpointVM[]> {
    const { data } = await axios.get('/auto-ui/api/endpoints')
    return data
}