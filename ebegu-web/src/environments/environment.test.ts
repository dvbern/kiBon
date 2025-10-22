import {LogLevel} from '@kibon/shared/util-fn/log-factory';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false
};
