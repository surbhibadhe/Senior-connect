import { useEffect, useRef } from 'react'
/** Moves an element horizontally as the page scrolls. Disabled for reduced motion. */
export function useScrollShift<T extends HTMLElement>(speed: number) {
  const ref = useRef<T>(null)
  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    let raf = 0
    const tick = () => { raf = 0; if (ref.current) ref.current.style.transform = `translate3d(${window.scrollY * speed}px,0,0)` }
    const onScroll = () => { if (!raf) raf = requestAnimationFrame(tick) }
    window.addEventListener('scroll', onScroll, { passive: true }); tick()
    return () => { window.removeEventListener('scroll', onScroll); if (raf) cancelAnimationFrame(raf) }
  }, [speed])
  return ref
}
