import { createStitches } from '@stitches/react';
// import {
//   teal,
//   violet,
//   crimson,
// } from '@radix-ui/colors';

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
	    // ...teal,
	    // ...violet,
	    // ...crimson,
	    // gray400: 'gainsboro',
	    // gray500: 'lightgray',
	},
	space: { // i * 3
	    1: '3px', // ~0.2rem
	    2: '6px', // ~0.4rem
	    3: '9px', // ~0.6rem
	    4: '12px', // ~0.8rem
	    5: '15px', // ~1rem
	    6: '18px',
	    7: '21px',
	    8: '24px',
	    9: '27px',
	    10: '30px',
	},
	fontSizes: {
	    // double-stranded modular scale
	    // ratio: 1:√3 (1.732), bases: 16, 14
	    // https://www.modularscale.com/?16,14&px&1.732
	    1: '0.875rem', // 14px
	    2: '1rem',     // 16px
	    3: '1.516rem', // 24.248px
	    4: '1.732rem', // 27.712px
	    5: '2.625rem', // 41.998px
	    6: '3rem',     // 47.997px
	    7: '4.546rem', // 72.74px
	    8: '5.196rem', // 83.131px
	    medium: '16px',
	    base: '14px',
	    small: '12px',
	},
	fonts: {
	    base: '"IBM Plex Sans", arial, sans-serif',
	    mono: '"IBM Plex Mono", courier, monospace',
	    // base: '"Berkeley Mono", "IBM Plex Sans", arial, sans-serif',
	    // mono: '"Berkeley Mono", "IBM Plex Mono", courier, monospace',
	},
	fontWeights: {
	    light: '200',
	    normal: '400',
	    medium: '600',
	    bold: '800',
	},
	lineHeights: {
	    base: '1.3em',
	},
	sizes: {
	    toolbarIcon: '18px',
	    inputIcon: '30px',
	    tabIcon: '36px',
	    hairline: '1px',
	},
	borderWidths: {
	    1: '1px',
	    2: '2px',
	    3: '3px',
	},
	radii: {
	    1: '2px',
	    2: '4px',
	    3: '6px',
	    4: '10px',
	    round: '50%',
	    pill: '9999px',
	},
    },
    media: {
	bp1: '(min-width: 480px)',
    },
    utils: {
    // marginX: (value) => ({ marginLeft: value, marginRight: value }),
  },
});


const scales = {
    s1:  '#FCFCFA',
    s2:  '#E6E3E1',
    s3:  '#D5D2CF',
    s4:  '#C4C0BE',
    s5:  '#B3AEAD',
    s6:  '#A19C9B',
    s7:  '#918C8C',
    s8:  '#797576',
    s9:  '#656363',
    s10: '#514F4F',
    s11: '#3E3D3C',
    s12: '#2A2A28',
    s13: '#1B1B13',

    n1:  '#14151F',
    n2:  '#2E3347',
    n3:  '#424659',
    n4:  '#55586B',
    n5:  '#666A7D',
    n6:  '#76798C',
    n7:  '#8A8D9E',
    n8:  '#9A9EAD',
    n9:  '#AEB1BF',
    n10: '#BEC2CF',
    n11: '#CED1DE',
    n12: '#E1E4F0',
    n13: '#F8F9FD',
}

const lightColors = {
    ...scales,

    outer_bg: scales.s2,
    outer_fg: scales.s12, // '#333231',
    outer_n100: scales.s7, // '#969493',
    outer_n200: scales.s4, // '#C2BEBE',
    outer_m100: scales.n2, // '#2F3347',
    outer_m200: scales.n6, // '#7A7E91',
    outer_hl: '#9297B0',
    outer_contrast: '#FFFBEB',

    fmenu_visu: '#C4D1CC',
    fmenu_calc: '#C8CAE0',
    fmenu_emul: '#DDC2CD',
    fmenu_base: '#D9D7D4',
    fmenu_glow: '#EDEBE8',

    inner_bg: scales.s1,
    inner_fg: scales.s11, // '#4A4847',
    inner_n100: scales.s6, // '#ADABAA',
    inner_n200: scales.s3, // '#D9D4D4',
    inner_m100: scales.n3, // '#3E445E',
    inner_m200: scales.n7, // '#8D92A8',
    inner_hl: '#FFFBEB',
    inner_contrast: '#FFFBEB',
    inner_visu: '#D8EBE2',
    inner_calc: '#E8E8FF',
    inner_emul: '#F5E1E8',
}

const darkColors = {
    ...scales,

    outer_bg: scales.n2, // '#2F3347',
    outer_fg: scales.n12,
    outer_n100: scales.n7, // '#848AA3',
    outer_n200: scales.n4, // '#5D627A',
    outer_m100: scales.s2, // '#E6E3E1',
    outer_m200: scales.s6, // '#A5A3A2',
    outer_hl: '#B6B9DB',
    outer_contrast: '#B6B9DB',

    fmenu_visu: '#516F6C',
    fmenu_calc: '#51537E',
    fmenu_emul: '#654E6A',
    fmenu_base: '$outer_n200',
    fmenu_glow: '$outer_n100',

    inner_bg: scales.n1, // '#14151F',
    inner_fg: scales.n11, // '#C5C7D1',
    inner_n100: scales.n6, // '#6B7085',
    inner_n200: scales.n3, // '#46495C',
    inner_m100: scales.s3, // '#C7C5C3',
    inner_m200: scales.s7, // '#878584',
    inner_hl: '#000', // '#9D9FBD',
    inner_contrast: '#9D9FBD',
    inner_visu: '#3B524F',
    inner_calc: '#3C3E5E',
    inner_emul: '#593447',
}

export const lightTheme = createTheme ('light-theme', {
    colors: lightColors,
});

export const darkTheme = createTheme('dark-theme', {
    colors: darkColors,
});

// const makeButtonColors = (theme) => {
//     button_primary_bg: theme.outer_m200,
//     button_secondary_bg: 'none',
// }

// export const lightTheme = createTheme ('light-theme', {
//     colors: {...lightColors,
// 	     ...makeButtonColors(lightColors)},
// }) ;

// export const darkTheme = createTheme('dark-theme', {
//     colors: {...darkColors,
// 	     ...makeButtonColors(darkColors)},
// });
