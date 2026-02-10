import {Game} from '../domain/Game'

export type SSEParams = {
    url: string;
    onMessage: (data: Game) => void;
    onError?: (error: Event) => void;
};

export function createSSEConnection(params: SSEParams): EventSource {
    const eventSource = new EventSource(params.url);

    eventSource.onmessage = (event) => {
        try {
            // console.log("Event", event);
            const data = JSON.parse(event.data);
            // console.log("Received parsed message", data);
            const game = JSON.parse(data.msg) as Game;
            // console.log("Received game", game);

            params.onMessage(game);
        } catch (e) {
            console.error("Failed to parse SSE data:", e);
        }
    };

    eventSource.onerror = (error) => {
        console.error("SSE error:", error);
        if (params.onError) {
            params.onError(error);
        }
        eventSource.close();
    };

    return eventSource;
}
