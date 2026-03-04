export interface Logger {
    debugEnabled: boolean;

    debug(message: string, ...optionalParams: any[]): any;

    log(message: string, ...optionalParams: any[]): any;

    error(message: string, error?: any, ...optionalParams: any[]): any;
}

export class ConsoleLogger implements Logger {
    constructor(public debugEnabled: boolean = true) {
    }

    debug(message: string, ...optionalParams: any[]): any {
        if (this.debugEnabled) {
            window.console.debug(message, optionalParams);
        }
    }

    log(message: string, ...optionalParams: any[]): any {
        window.console.log(message, optionalParams);
    }

    error(message: string, error?: any, ...optionalParams: any[]): any {
        window.console.error(message, error, optionalParams);
    }
}

const logger: Logger = new ConsoleLogger();

export default logger;
