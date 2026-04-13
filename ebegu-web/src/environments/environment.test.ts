import {LogLevel} from '@utils/log';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false
};
