import { createStitches } from '@stitches/react';
import {
  teal,
  violet,
  crimson,
} from '@radix-ui/colors';

export const {
  styled,
  css,
  globalCss,
  keyframes,
  getCssText,
  theme,
  createTheme,
  config,
} = createStitches({
  theme: {
    colors: {
      ...teal,
      ...violet,
      ...crimson,
      gray400: 'gainsboro',
      gray500: 'lightgray',
      lime: '#e6ff8d',
      accent: '$teal7',
    },
  },
  media: {
    bp1: '(min-width: 480px)',
  },
  utils: {
    marginX: (value) => ({ marginLeft: value, marginRight: value }),
  },
});
