import {LogLevel} from '@kibon/shared/util-fn/log-factory';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: false,
    hmr: true,
    logLevel: LogLevel.INFO,
    logModules: {}
};
