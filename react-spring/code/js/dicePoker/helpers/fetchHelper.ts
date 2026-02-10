export type RequestParams = {
    url: string;
    method: string;
    headers?: { [key: string]: string };
    body?: { [key: string]: string|number | string[] | number[] };
    jsonResponse?: boolean;  // some responses we may need to check the headers for location
}

export async function fetchHelper(
    request: RequestParams,
) {
    // Fetch helper
    // console.log("FetchHelper request", request);
    const jsonResponse = request.jsonResponse ?? true // this evaluates if jsonResponse is not null or undefined and if it is returns null else the left side
    return fetch(
        request.url,
        {
            method: request.method || 'GET', // default to GET
            headers: request.headers || {"Content-Type": "application/json"},  // if no headers default to json
            body: JSON.stringify(request.body),
        }
    ).then(
        response => {
            // console.log("Response body", response)
            // return response.json()
            return jsonResponse ? response.json() : response
        }
    ).catch(
        error => {
            console.log("Error occurred while making a request:" +
                "url: " + request.url +
                "header" + request.headers +
                "body: " + request.body ? JSON.stringify(request.body) : "null" +
                "error: " + error.message
            );
        }
    )
}

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Nullish_coalescing