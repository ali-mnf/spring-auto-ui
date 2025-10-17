export type InputVM = {
    name: string
    source: 'PATH_VARIABLE' | 'REQUEST_PARAM' | 'REQUEST_HEADER' | 'REQUEST_BODY'
    type: string | null
    required: boolean
    defaultValue?: string | null
    exampleJson?: string | null
}


export type EndpointVM = {
    id: string
    controller: string
    controllerFqn: string
    javaMethodName: string | null
    httpMethod: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS'
    paths: string[]
    consumes: string[]
    produces: string[]
    headers?: string[]
    inputs: InputVM[]
    output: { returnClass: string | null, fields: string[] }
}